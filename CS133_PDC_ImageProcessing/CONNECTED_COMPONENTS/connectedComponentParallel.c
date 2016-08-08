#include <stdio.h>
#include <stdlib.h>
#include <omp.h>

#include "bmpfile.h"


#define PEAK_SPACE            50
#define PEAKS                 30
#define GRAY_LEVELS          255
#define STACK_SIZE         400000

//Stores the number of columns and rows in the image
int ip_width, ip_height;

/******************** Connected Component Labelling Parallel Code ************************************/
/****** Identifying connected pixel regions, i.e. regions of adjacent pixels which share the ***********/
/****** same set of intensity values (chosen between 0-255)                                  ***********/

/** Storing the obtained peak values and their corresponding location for adaptive histogram segmentation **/
void insert_into_peaks(int peaks[PEAKS][2], int max, int max_place){
   int i, j;

      /* first case */
   if(max > peaks[0][0]){
      //too small to parallelize
      for(i=PEAKS-1; i>0; i--){
         peaks[i][0] = peaks[i-1][0];
         peaks[i][1] = peaks[i-1][1];
      }
      peaks[0][0] = max;
      peaks[0][1] = max_place;
   }  /* ends if */

      /* middle cases */
   for(j=0; j<PEAKS-3; j++){
      if(max < peaks[j][0]  && max > peaks[j+1][0]){
         for(i=PEAKS-1; i>j+1; i--){
            peaks[i][0] = peaks[i-1][0];
            peaks[i][1] = peaks[i-1][1];
         }
         peaks[j+1][0] = max;
         peaks[j+1][1] = max_place;
      }  /* ends if */
   }  /* ends loop over j */
      /* last case */
   if(max < peaks[PEAKS-2][0]  && 
      max > peaks[PEAKS-1][0]){
      peaks[PEAKS-1][0] = max;
      peaks[PEAKS-1][1] = max_place;
   }  /* ends if */

}  /* ends insert_into_peaks */

/** Finding the threshold hi and low values **/
void find_peaks(unsigned long histogram[], int *peak1, int *peak2){
   int distance[PEAKS], peaks[PEAKS][2];
   int i, j=0, max=0, max_place=0;

   #pragma omp parallel for private(i)
   for(i=0; i<PEAKS; i++){
      distance[i] =  0;
      peaks[i][0] = -1;
      peaks[i][1] = -1;
   }
   #pragma omp parallel for private(i, max, max_place)
   for(i=0; i<=GRAY_LEVELS; i++){
      max       = histogram[i];
      max_place = i;
      insert_into_peaks(peaks, max, max_place);
   }  /* ends loop over i */
 
   #pragma omp parallel for private(i)
   for(i=1; i<PEAKS; i++){
      distance[i] = peaks[0][1] - peaks[i][1];
      if(distance[i] < 0)
         distance[i] = distance[i]*(-1);
   }

   *peak1 = peaks[0][1];
   for(i=PEAKS-1; i>0; i--)
    if(distance[i] > PEAK_SPACE) *peak2 = peaks[i][1];

}  /* ends find_peaks */

/** Find hi and low values so that threshold the image around the smaller of the two "humps" in the
    histogram.  This is because the smaller hump represents the objects while the larger hump represents the background. **/
void peaks_high_low(unsigned long histogram[], int peak1, int peak2, int *hi, int *low){
   int i, mid_point;
   unsigned long sum1 = 0, sum2 = 0;

   if(peak1 > peak2)
      mid_point = ((peak1 - peak2)/2) + peak2;
   if(peak1 < peak2)
      mid_point = ((peak2 - peak1)/2) + peak1;

   #pragma omp parallel for reduction(+:sum1)
   for(i=0; i<mid_point; i++)
      sum1 = sum1 + histogram[i];
	  
   #pragma omp parallel for reduction(+:sum2)
   for(i=mid_point; i<=GRAY_LEVELS; i++)
      sum2 = sum2 + histogram[i];
   if(sum1 >= sum2){
      *low = mid_point;
      *hi  = GRAY_LEVELS;
   }
   else{
      *low = 0;
      *hi  = mid_point;
   }

}  /* ends peaks_high_low */

/** Finds the object and background difference intensities **/
void threshold_and_find_means(int **gs_image, int **out_image, int hi, int low, int value, int *object_mean, int *background_mean){
   int      counter = 0, i, j;
   unsigned long object     = 0,
                 background = 0;

   #pragma omp parallel for private(i,j)
   for(i=0; i<ip_width; i++){
      for(j=0; j<ip_height; j++){
	
         if(gs_image[i][j] >= low  && gs_image[i][j] <= hi){
            out_image[i][j] = value;
			#pragma omp critical
			{
				counter++;
				object = object + gs_image[i][j];
			}
         }
         else{
            out_image[i][j] = 0;
			#pragma omp critical
            background      = background + gs_image[i][j];
         }
	
      }  /* ends loop over j */
   }  /* ends loop over i */
   
   object     = object/counter;
   background = background/((ip_width*ip_height)-counter);
   *object_mean     = (int)(object);
   *background_mean = (int)(background);
  
}  /* ends threshold_and_find_means */

/** Takes the input image and applies thresholding with hi and low values **/
/** If the value lies in the range of high and low the pixel is set to intensity value else set to 0 **/
void threshold_image_array(int **gs_image, int **out_image,int hi,int low,int value){
   int   counter = 0, i, j;
   #pragma omp parallel for private(i,j)
   for(i=0; i<ip_width; i++){
      for(j=0; j<ip_height; j++){
         if(gs_image[i][j] >= low  &&
            gs_image[i][j] <= hi){
            out_image[i][j] = value;
			#pragma omp critical
            counter++;
         }
         else
            out_image[i][j] = 0;
	
      }  /* ends loop over j */
   }  /* ends loop over i */
  
}  /* ends threshold_image_array */

/** Checks the 8 neighbors for a pixel and labels it accordingly **/
void label_and_check_neighbor(int **binary_image, int stack[STACK_SIZE][2], int g_label, int *stack_empty, int *pointer, int r, int e, int value){
  
   int already_labeled = 0,i, j;
   int selected = 0;

   if (binary_image[r][e] == g_label){
      already_labeled = 1;
      //return 0;
    }
   binary_image[r][e] = g_label;

     
	#pragma omp parallel for private(i,j)
   for(i=(r-1); i<=(r+1); i++){
      for(j=(e-1); j<=(e+1); j++){
	
         if((i>=0) && (i<=(ip_width-1))  && (j>=0)   && (j<=(ip_height-1))){
		if(binary_image[i][j] == value && i!=r && j!=e){
			selected = 1;
		}
	}
      }
   }
   
   for(i=(r-1); i<=(r+1); i++){
      for(j=(e-1); j<=(e+1); j++){
	 if((i>=0) && (i<=(ip_width-1))  && (j>=0)   && (j<=(ip_height-1))){
	    if(binary_image[i][j] == value){
		if(i!=r && j!=e){
		       *pointer           = *pointer + 1;
		       stack[*pointer][0] = i; /* PUSH      */
		       stack[*pointer][1] = j; /* OPERATION */
		       *stack_empty       = 0;
			
		}else{
		    if(selected ==1){
		   	 *pointer           = *pointer + 1;
		       stack[*pointer][0] = i; /* PUSH      */
		       stack[*pointer][1] = j; /* OPERATION */
		       *stack_empty       = 0;
			
		    }
                
                }
            }  /* end of if binary_image == value */
         }  /* end if i and j are on the image */
      }  /* ends loop over i ip_width           */
   }  /* ends loop over j columns        */
}  /* ends label_and_check_neighbors  */


/** Find the connected components **/
void connected_components(int **binary, int value){
   char name[80];

   int first_call,
       i1,
       j1,
       object_found,
       pointer,
       pop_i,
       pop_j,
       stack_empty;

   int g_label, stack[STACK_SIZE][2];

   g_label       = 2;
   object_found  = 0;
   first_call    = 1;
   for(i1=0; i1<ip_width; i1++){
      for(j1=0; j1<ip_height; j1++){
	      stack_empty       =  1;
         pointer           = -1;

         if(binary[i1][j1] == value){
	    
            label_and_check_neighbor(binary, stack, 
                      g_label, &stack_empty, &pointer, 
                      i1, j1, value);
            object_found = 1;
         }  /* ends if binary[i]j] == value */


         while(stack_empty == 0){
            pop_i = stack[pointer][0]; /* POP       */
            pop_j = stack[pointer][1]; /* OPERATION */
	     --pointer;
            if(pointer <= 0){
                  pointer     = 0;
                  stack_empty = 1;
               }  
              /*  ends if point <= 0  */

            label_and_check_neighbor(binary, stack, g_label, &stack_empty, &pointer, pop_i,
                        pop_j, value);
         }  /* ends while stack_empty == 0 */
	    if(object_found == 1){
            object_found = 0;
            ++g_label;
         }  /* ends if object_found == 1 */

      }   /* ends loop over j */
   }  /* ends loop over i */

   printf("\nConnected_components> found %d objects", g_label);

} /* ends connected_components  */



/*** Main function ***/
int main( int argc, char **argv ) 
{
    FILE *inFile;
    int i = 0;
    bmpfile_t *in_image = NULL, *out_image = NULL;
    rgb_pixel_t *rgb_pixel = NULL;

	//input file name for image
 	char* file_name = argv[1];
	//number of threads
	int threads = atoi(argv[2]);

 	in_image = bmp_create_8bpp_from_file(file_name);
	out_image = bmp_create_8bpp_from_file(file_name);

 	ip_width = bmp_get_width(in_image);
 	ip_height = bmp_get_height(in_image);

 	int **gs_image, **bin_image;
 
 	int x,y;
 	gs_image = (int**)malloc(sizeof(int*) * ip_width);
	bin_image = (int**)malloc(sizeof(int*) * ip_width);
	
	omp_set_num_threads(threads);
	
	//converting to grayscale
	#pragma omp parallel for private(x,y, rgb_pixel)
 	for(x=0;x<ip_width;x++)
 	{
 	 	gs_image[x] = (int*)malloc(sizeof(int) * ip_height);
		bin_image[x] = (int*)malloc(sizeof(int) * ip_height);
 		for(y=0;y<ip_height;y++)
 		{
			rgb_pixel = bmp_get_pixel(in_image, x, y);
			int grayscale = rgb_pixel->red * 0.299 + rgb_pixel->green * 0.587 + rgb_pixel->blue * 0.114;
			gs_image[x][y] = grayscale;
			bin_image[x][y] = grayscale;
			
 		}
 	}

	unsigned long new_hist[256];
	unsigned long histogram[256];
	int c,grayValue,r;
	int gray_levels = 256;
	int peak1, peak2;
	int    background, hi, low, object, value = 120;
	
    /*------INITIALIZE ARRAY------*/
  #pragma omp parallel for private(i)
  for(i=0; i<=256; i++) histogram[i] = 0;
  #pragma omp parallel for private(i)
  for(i=0; i<=255; i++) new_hist[i] = 0;
 
    for(r=0; r<ip_width; r++) {
	    for(c=0; c<ip_height; c++)  {
	      grayValue = gs_image[r][c];
		//printf("grayscale val %d\n", gs_image[r][c]);
	      histogram[grayValue] = histogram[grayValue] + 1;
	    }
   }
   
   
   //smoothing histogram
   new_hist[0] = (histogram[0] + histogram[1])/2;
   new_hist[gray_levels] =
      (histogram[gray_levels] +
       histogram[gray_levels-1])/2;

	#pragma omp parallel for private(i)
	for(i=1; i<gray_levels-1; i++){
      new_hist[i] = (histogram[i-1] +
                     histogram[i]   +
                     histogram[i+1])/3;
   }

   #pragma omp parallel for private(i)
   for(i=0; i<gray_levels; i++)
      histogram[i] = new_hist[i];

	//Thresholding the image based on adaptive histogram segmentation
   find_peaks(histogram, &peak1, &peak2);
   peaks_high_low(histogram, peak1, peak2, &hi, &low);
   threshold_and_find_means(gs_image, bin_image, hi, low, value, &object, &background);
   peaks_high_low(histogram, object, background, &hi, &low);
   threshold_image_array(gs_image, bin_image, hi, low, value);
   
   #pragma omp parallel for private(x,y,rgb_pixel)
   for(x=0;x<ip_width;x++){
	for(y=0;y<ip_height;y++){
		//printf("binimage %d %d %d\n", x, y, bin_image[x][y]);
		rgb_pixel = bmp_get_pixel(out_image, x, y);
		rgb_pixel->red = bin_image[x][y];
		rgb_pixel->green = bin_image[x][y];
		rgb_pixel->blue = bin_image[x][y];
		bmp_set_pixel(in_image, x, y, *rgb_pixel);
	}
   }
   bmp_save(out_image, "concomplblPll.bmp"); 		

   //segmentation to label connected component
   connected_components(bin_image, value);

    //prints the labelled connected components
	for(x=0;x<ip_width;x++){
	for(y=0;y<ip_height;y++){
	   printf("%d ", bin_image[x][y]);
	}
	printf("\n");
    }

	free(gs_image);
	free(bin_image);
    bmp_destroy(in_image);
    bmp_destroy(out_image);

    return 0;
}

