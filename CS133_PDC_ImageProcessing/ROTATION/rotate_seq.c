#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include "bmpfile.h"
#define PI 3.14159265
/******************************************
      *
      *   This is ROTATING AN IMAGE IN A SEQUENTIAL WAY
      *
      *   INPUTS:
      *			image_name
      *			angle_of_rotation
      *   OUTPUT:
      *			ImageFile that is rotated by certain degree
      *
      *   It only works for 8-bit images.
      *
      ******************************************/
#pragma pack(2)


int main( int argc, char **argv ) 
{
// To measure the execution time
	double start = omp_get_wtime( );
    int i = 0;

// Input parameter are image_name and angle of rotation     
 	char* file_name = argv[1];
 	double angle =atof(argv[2]);
 	
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

// Calculating maximum width and height when an image is rotated at an angle other than 90, 180, 360. Image size will be increased for rotating around other degree 	
 	for(x=ip_width-1;x>=0;x--)
 	{
 		for(y=ip_height-1;y>=0;y--)
 		{
			x1=x*c-y*s-m*c+n*s+m;//rotating about any point ...
			y1=y*c+x*s-m*s-n*s+n;
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
 	
 	
 	int out_width=maxx1-minx1;
    int out_height=maxy1-miny1;

// Creating image with new width and height    
    bmp_op=bmp_create(out_height, out_width, ip_depth);
    bmp_op=bmp_create(maxx1-minx1, maxy1-miny1, ip_depth);

// Setting each pixel of new image with colors from old image    
	for(x=ip_width-1;x>=0;x--)
 	{
 		for(y=ip_height-1;y>=0;y--)
 		{
			x1=x*c-y*s-m*c+n*s+m;//rotating about any point
			y1=y*c+x*s-m*s-n*s+n;
		
		bmp_set_pixel(bmp_op, x1-minx1, y1-miny1, *get_8bpp_color(bmp_read, find_closest_color(bmp_read, *bmp_get_pixel(bmp_read, x, y))));
		}
 	}
 
//Saving the result image
 	bmp_save(bmp_op, "bmp_rotate.bmp"); 	
 	
	bmp_destroy(bmp_read);
// Finding total execution time	
	double end = omp_get_wtime( );
	printf("Execution Time = %.16g\n", end - start);
    return 0;
}

