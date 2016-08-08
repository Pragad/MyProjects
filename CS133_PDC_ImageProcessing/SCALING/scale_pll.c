#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include <omp.h>
#include "bmpfile.h"
#include<time.h>
#include<unistd.h>
#include <sys/timeb.h>
#include<sys/time.h>

/******************************************
      *
      *   This is SCALAING AN IMAGE IN A PARALLEL WAY
      *
      *   INPUTS:
      *			image_name
      *			scale_factor_x
      *			scale_factor_y
      *			number_of_threads
      *			chunk_size [for dynamic scheduling]
      *   OUTPUT:
      *			ImageFile that is either scaled down or scaled up
      *
      *   It only works for 8-bit images.
      *
      ******************************************/
#pragma pack(2)

int main( int argc, char **argv ) 
{
    FILE *inFile;
    int i = 0;
	struct timeval tp1,tp2;
	struct timeb tm;

// Input parameter are image_name, scaling factor along width, scaling factor along height, number of threads and chunk_size     
 	char* file_name = argv[1];
 	char* s1=argv[2];
 	char* s2=argv[3];
 	double sx=atof(s1);
 	double sy=atof(s2);
	int chunksize=atoi(argv[5]);
	
// Reading the image file and obtaining its width height and depth		
 	bmpfile_t *bmp_read;
 	bmp_read = bmp_create_8bpp_from_file(file_name);
	bmpfile_t *bmp_op;
	
 	int ip_width = bmp_get_width(bmp_read);
 	int ip_height = bmp_get_height(bmp_read);
 	int ip_depth = bmp_get_depth(bmp_read);
   
    int number_threads;
   
// Calculating new width and height of the image
    int out_width=sx*ip_width;
    int out_height=sy*ip_height;
    bmp_op=bmp_create(out_width, out_height, ip_depth);

    
    int x=0,y=0;
    int x1=0,y1=0;

 	int** color_table;
 	int m,n;
	number_threads = atoi(argv[4]);
 		
 	color_table = (int**)malloc(sizeof(int*) * ip_width);
	omp_set_num_threads(number_threads);
	
	gettimeofday(&tp1, NULL);
	ftime (&tm);
	tp1.tv_sec = tm.time;
	tp1.tv_usec = tm.millitm * 1000;

// For scaling we start from the bottom right corner of the image
#pragma omp parallel private(y,x1,y1,m,n)
{
#pragma omp for schedule(dynamic,chunksize) nowait
 	for(x=ip_width-1;x>=0;x--)
 	{
 	 	color_table[x] = (int*)malloc(sizeof(int) * ip_height);
 		for(y=ip_height-1;y>=0;y--)
 		{
			color_table[x][y] = find_closest_color(bmp_read, *bmp_get_pixel(bmp_read, x, y));
			x1=(int)x*sx;
			y1=(int)y*sy;	
	
		for(n=y1;n<sy*(y+1);n++)
				for(m=x1;m<sx*(x+1);m++)
					bmp_set_pixel(bmp_op, m, n, *get_8bpp_color(bmp_read, color_table[x][y]));

		}
 	}
 }	
 // Finding total execution time		
 gettimeofday(&tp2, NULL);
 ftime (&tm);
 tp2.tv_sec = tm.time;
 tp2.tv_usec = tm.millitm * 1000;

printf("Elapsed time = %d \n",(tp2.tv_sec-tp1.tv_sec)*1000000 + (tp2.tv_usec-tp1.tv_usec));
 	bmp_save(bmp_op, "bmp_scale.bmp"); 	
 	
   
	bmp_destroy(bmp_read);
	for (i = 0; i < ip_width; i++) 
	{
		free(color_table[i]);
	}
	free(color_table);
	
    return 0;
}

