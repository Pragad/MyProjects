#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include "bmpfile.h"
#include <limits.h>
#include <omp.h>
/******************************************
      *
      *   This is SCALAING AN IMAGE IN A SEQUENTIAL WAY
      *
      *   INPUTS:
      *			image_name
      *			scale_factor_x
      *			scale_factor_y
      *			
      *   OUTPUT:
      *			ImageFile that is either scaled down or scaled up
      *
      *   It only works for 8-bit images.
      *
      ******************************************/
#pragma pack(2)

int main( int argc, char **argv ) 
{

	double start = omp_get_wtime( );
    FILE *inFile;
    int i = 0;

 	char* file_name = argv[1];
 	char* s1=argv[2];
 	char* s2=argv[3];
 	double sx=atof(s1);
 	double sy=atof(s2);

 	bmpfile_t *bmp_read;
 	bmp_read = bmp_create_8bpp_from_file(file_name);
	bmpfile_t *bmp_op;
	
 	int ip_width = bmp_get_width(bmp_read);
 	int ip_height = bmp_get_height(bmp_read);
 	int ip_depth = bmp_get_depth(bmp_read);
    int number_threads;
    int out_width=sx*ip_width;
    int out_height=sy*ip_height;
    bmp_op=bmp_create(out_width, out_height, ip_depth);
    
    int x=0,y=0;
    int x1=0,y1=0;


 	int m,n;
 		
 	for(x=ip_width-1;x>=0;x--)//2,1,0
 	{
 		for(y=ip_height-1;y>=0;y--)//1,0
 		{
			x1=(int)x*sx;
			y1=(int)y*sy;	
	
		for(n=y1;n<sy*(y+1);n++)
		{
				for(m=x1;m<sx*(x+1);m++)
				{
					bmp_set_pixel(bmp_op, m, n, *get_8bpp_color(bmp_read, find_closest_color(bmp_read, *bmp_get_pixel(bmp_read, x, y))));
				}
		}

		}
 	}
 	bmp_save(bmp_op, "bmp_write.bmp"); 	
 	
	bmp_destroy(bmp_read);
	double end = omp_get_wtime( );
	printf("Execution Time = %.16g\n", end - start);
    return 0;
}

