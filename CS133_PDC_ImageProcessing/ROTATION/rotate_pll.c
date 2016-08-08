#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include "bmpfile.h"
#include <omp.h>
#include<time.h>
#include<unistd.h>
#include <sys/timeb.h>
#include<sys/time.h>

#define PI 3.14159265
/******************************************
      *
      *   This is ROTATING AN IMAGE IN A PARALLEL WAY
      *
      *   INPUTS:
      *			image_name
      *			angle_of_rotation
      *			number_of_threads
      *			chunk_size [for dynamic scheduling]
      *   OUTPUT:
      *			ImageFile that is rotated by certain degree
      *
      *   It only works for 8-bit images.
      *
      ******************************************/
#pragma pack(2)
int main( int argc, char **argv ) 
{
       int i = 0;
// To measure the execution time
	struct timeval tp1,tp2;
	struct timeb tm;

// Input parameter are image_name, angle of rotation, number of threads and chunk_size     
 	char* file_name = argv[1];
 	double angle =atof(argv[2]);
 	int T=atoi(argv[3]);
	int chunksize=atoi(argv[4]);
	
// Reading the image file and obtaining its width height and depth	
 	bmpfile_t *bmp_read;
 	bmp_read = bmp_create_8bpp_from_file(file_name);
	bmpfile_t *bmp_op;
	
 	int ip_width = bmp_get_width(bmp_read);
 	int ip_height = bmp_get_height(bmp_read);
 	int ip_depth = bmp_get_depth(bmp_read);
    
// Converting angle to appropriate formate for using in the code        
    angle=-angle;
    double c=cos(angle*PI/180);
	double s=sin(angle*PI/180);
    double t=s/c;
    
    
    int x=0,y=0;
    int x1=0,y1=0;
    int m,n;
    m=ip_width/2;
    n=ip_height/2;
 	int minx1=INT_MAX,miny1=INT_MAX,maxx1=INT_MIN,maxy1=INT_MIN;
 	omp_set_num_threads(T);
	gettimeofday(&tp1, NULL);
	ftime (&tm);
	tp1.tv_sec = tm.time;
	tp1.tv_usec = tm.millitm * 1000;

// Calculating maximum width and height when an image is rotated at an angle other than 90, 180, 360. Image size will be increased for rotating around other degree 	
 	#pragma omp parallel private(y,x1,y1) 
 	{
 		#pragma omp for 
			for(x=ip_width-1;x>=0;x--)//2,1,0
			{
				for(y=ip_height-1;y>=0;y--)//1,0
				{
					x1=x*c-y*s-m*c+n*s+m;//rotating about any point ...formula taken from book
					y1=y*c+x*s-m*s-n*s+n;//
					if(x1<minx1)
					minx1=x1;
					if(y1<miny1)
					miny1=y1;
					if(x1>maxx1)
					maxx1=x1;
					if(y1>maxy1)
					maxy1=y1;
				}
			}
 	}
 	int out_width=maxx1-minx1;
    int out_height=maxy1-miny1;
    
// Creating image with new width and height   
    bmp_op=bmp_create(out_height, out_width, ip_depth);
    bmp_op=bmp_create(maxx1-minx1, maxy1-miny1, ip_depth);
    
// Setting each pixel of new image with colors from old image      
    #pragma omp parallel private(y,x1,y1)
    {
    #pragma omp for 
		for(x=ip_width-1;x>=0;x--)//2,1,0
	 	{
	 		for(y=ip_height-1;y>=0;y--)//1,0
	 		{
				x1=x*c-y*s-m*c+n*s+m;//rotating about any point ...formula taken from book
				y1=y*c+x*s-m*s-n*s+n;//
		
			bmp_set_pixel(bmp_op, x1-minx1, y1-miny1, *get_8bpp_color(bmp_read, find_closest_color(bmp_read, *bmp_get_pixel(bmp_read, x, y))));
			}
	 	}
 	}
 	
// Finding total execution time		
 gettimeofday(&tp2, NULL);
 ftime (&tm);
 tp2.tv_sec = tm.time;
 tp2.tv_usec = tm.millitm * 1000;

printf("Elapsed time = %d \n",(tp2.tv_sec-tp1.tv_sec)*1000000 + (tp2.tv_usec-tp1.tv_usec));
//Saving the result image
 	bmp_save(bmp_op, "bmp_rotate.bmp"); 	

//Freeing memory
	bmp_destroy(bmp_read);

    return 0;
}

