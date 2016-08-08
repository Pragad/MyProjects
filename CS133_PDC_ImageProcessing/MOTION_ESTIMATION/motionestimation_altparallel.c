#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include "bmpfile.h"
#include <omp.h>
#define max_block 20
#define p 10
#define THRESHOLD 5000

bmpfile_t *bmp_original = NULL, *bmp_read = NULL;

//Function to draw line between two points Bresenham's line algorithm

void drawLine(int x1, int y1, int x2, int y2)
{
     
	int x,y,dx,dy,p1,end;	
	rgb_pixel_t *rgb_pixel = NULL;
	int i,j;
 
	dx = abs(x1 - x2);
	dy = abs(y1 - y2);
	p1 = 2 * dy - dx;
	if(x1 > x2)
	{
		x = x2;
		y = y2;
		end = x1;
	}
	else
	{
		x = x1;
		y = y1;
		end = x2;
	}
	rgb_pixel = bmp_get_pixel(bmp_read, x, y);
	rgb_pixel->red = 255;
	rgb_pixel->green = 0;
	rgb_pixel->blue = 0;
	while(x < end)
	{
		x = x + 1;
		if(p1 < 0)
		{
		      p1 = p1 + 2 * dy;
		}
		else
		{
		      y = y + 1;
		      p1 = p1 + 2 * (dy - dx);
		}
		rgb_pixel = bmp_get_pixel(bmp_read, x, y);
		rgb_pixel->red = 255;
		rgb_pixel->green = 0;
		rgb_pixel->blue = 0;
                
	}
    bmp_set_pixel(bmp_read, x, y, *rgb_pixel);    
    //for drawing vector
 /*  for(i=0;i<3;i++)
    {
     for(j=0;j<3;j++)
      {
       if(x2>=1&&y2>=1)
        bmp_set_pixel(bmp_read, x2+i-1, y2+j-1, *rgb_pixel);
      }
    }*/
 	
}


int main( int argc, char **argv ) 
{
    double start,end;
    FILE *inFile;
 
    int i = 0;

    char* source = argv[1];
    char* search = argv[2];	
    int nT = atoi(argv[3]);
     start = omp_get_wtime();
    bmpfile_t *bmp_source;
    bmpfile_t *bmp_search;
    bmp_source = bmp_create_8bpp_from_file(source);
    bmp_search = bmp_create_8bpp_from_file(search);
	if(bmp_search == NULL||bmp_source == NULL)
	   printf("BMP NULL\n");
	
    bmp_read = bmp_create_8bpp_from_file(source);
      
     
    int ip_width = bmp_get_width(bmp_source);
    int ip_height = bmp_get_height(bmp_source);
 	
  
      
    int flag,j,m,n,refVer,refHor,refHorminx, temp_i, temp_j,min_i,min_j;
    float err=0,min_cost=INT_MAX, same_pos_cost=INT_MAX;
    float check,check_loop=INT_MAX;
    int color_source,color_search;
    int minx=0,miny=0;
    omp_set_num_threads(nT);

//select one block starting with i,j pixel
    // parallelizing the outermost for loop.
   for(i=0;i<ip_width-max_block+1;i=i+max_block)
   {     
     #pragma omp parallel for private(j,m,n,refVer,refHor,temp_i,temp_j,min_cost,err,color_source,color_search,same_pos_cost,min_i,min_j,check,check_loop) 
     for(j=0;j<ip_height-max_block+1;j=j+max_block)
     {
       //check for the matching block in the reference image. window size is 2p+1
        for(m=-p;m<=p;m++)
        {
         for(n=-p;n<=p;n++)
            {
           
              //refVer and refHor are the bigger BOUNDARIES that has to be searched for identical blocks
              refVer = i + m;   
              refHor = j + n;   
           
	      //check to see if the block in reference image is inside the image.
              if ( refVer < 0|| refVer+max_block-1 >= ip_width|| refHor < 0 || refHor+max_block-1 >= ip_height)
               {
                   continue;
               }
              err=0;
	     
              //Searches till the MAX Block i.e. each pixel of a block from 0
             temp_i=i;temp_j=j;
              

          //calculate the cost difference, Mean Absolute Difference between current block and reference block 
	     
              for(temp_i=i;temp_i < i + max_block;temp_i++,refVer++)
              {
		
         	   for(temp_j=j,refHor=j+n;temp_j < j + max_block;temp_j++,refHor++) 
           		{          				      
			   	    color_source=find_closest_color(bmp_source, *bmp_get_pixel(bmp_source, temp_i, temp_j));
				    rgb_pixel_t *temp = bmp_get_pixel(bmp_search,refVer,refHor);
				    color_search=find_closest_color(bmp_search, *temp);
				    err+=fabs(color_source-color_search);
				    //to optimise. if the cost anytime exceeds the minimum cost or threshold
				    if(err>check_loop||err>THRESHOLD)
				      break;
               		}                              
               	    if(temp_j!=(j+max_block))
		      break;
	                
              }
              
             if(m==0&&n==0)
               {
                same_pos_cost=err;
               }
               
              if(temp_i!=(i+max_block))
		   continue;
              check=err;
             
              err=err/(max_block*max_block);
             
          

	      if(min_cost>err)
        	 {
         
		   min_cost=err;
		   check_loop=check;
		   min_i=i+m;
		   min_j=j+n;
	         }  
             if(min_cost==0.0)
               break;                          
	      }
             if(min_cost==0.0)
               break;  
         }
    //draw line only if the minimum cost block in reference image is not at the same position as the current image block
         if(same_pos_cost > min_cost )
             {   
	       //call line drawing function between the two blocks which match closest
	          drawLine(i,j,min_i,min_j);
            
	     }
	    
	      check_loop=INT_MAX;
	      same_pos_cost=INT_MAX;
	      min_cost = INT_MAX;
      }
   }
   //save the image	  
   bmp_save(bmp_read, "line_new.bmp");   
   end = omp_get_wtime( );
   printf("Execution Time = %.16g\n", end - start);
    return 0;
}


