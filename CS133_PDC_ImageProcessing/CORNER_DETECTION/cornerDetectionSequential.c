/* Corner Detection using Harris/Plessey coner detector algorithm */

#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "bmpfile.h"

/*
Declaring threshold to be 400000 kept as per the image. r is used to determine the window in which a corner is detected. It is kept 3x3 here. k is used for calculating the cornerness measure for every pixel. 
*/
float threshold = 400000;
float k = 0.04;
int r = 3;

//function for performing convolution
float convolve(float** diff, float gaussian_kernel[3][3], int i, int j)
{
	float sum=0;
	float this_kernel, this_diff;
	int m=0, n=0;
	
	for(m=0;m<3;m++)
	{
		for(n=0;n<3;n++)
		{
			this_kernel = gaussian_kernel[m][n];

			this_diff = diff[i+(m-1)][j+(n-1)];
			
			sum += this_kernel * this_diff;
		}
	}

	return sum;
}


int main(int argc, char* argv[])
{
	char* file_name = argv[1];
	bmpfile_t *bmp_ip=NULL;
	bmpfile_t *bmp_op=NULL;

	float hred, vred, A, B, C, det_M, trace_M, Iv, Ih;
	int i=0, j=0;

	bmp_ip = bmp_create_8bpp_from_file(file_name);

	int ip_width = bmp_get_width(bmp_ip);
	int ip_height = bmp_get_height(bmp_ip);
	int ip_depth = bmp_get_depth(bmp_ip);

	bmp_op = bmp_create(ip_width, ip_height, ip_depth);

	//defining the gaussian kernel
	float gaussian_kernel[3][3];
	gaussian_kernel[0][0] = 0.04;
	gaussian_kernel[0][1] = 0.12;
	gaussian_kernel[0][2] = 0.04;
	gaussian_kernel[1][0] = 0.12;
	gaussian_kernel[1][1] = 0.36;
	gaussian_kernel[1][2] = 0.12;
	gaussian_kernel[2][0] = 0.04;
	gaussian_kernel[2][1] = 0.12;
	gaussian_kernel[2][2] = 0.04;

	/*
	temporary pixel to assign to a pixel in the new image if the value gotten from the original image is null
	*/
	rgb_pixel_t *temp_pixel = (rgb_pixel_t*)malloc(sizeof(rgb_pixel_t ));
	temp_pixel->red = 0;
	temp_pixel->blue =0;
	temp_pixel->green =0;

	//Declaring arrays to store differentials along x, y, and diagonal direction as per the algorithm
	float** diffx;
	float** diffy;
	float** diffxy;
	
	diffx = (float**)malloc(sizeof(float*)*ip_width);
	diffy = (float**)malloc(sizeof(float*)*ip_width);
	diffxy = (float**)malloc(sizeof(float*)*ip_width);

	for (i = 0; i < ip_width; i++) //rows
	{
		diffx[i] = (float*)malloc(sizeof(float)*ip_height);
		diffy[i] = (float*)malloc(sizeof(float)*ip_height);
		diffxy[i] = (float*)malloc(sizeof(float)*ip_height);

		for (j = 0; j < ip_height; j++) //columns
		{

			//First copying the image as is as the output image
			rgb_pixel_t *copy_pixel = bmp_get_pixel(bmp_ip, i,j);
			bmp_set_pixel(bmp_op, i, j, *copy_pixel);

			//getting hold of the pixels on the right and left of a pixel in consideration
			rgb_pixel_t *pixelx1 = bmp_get_pixel(bmp_ip, i-1, j)==NULL?temp_pixel:bmp_get_pixel(bmp_ip, i-1, j);
			rgb_pixel_t *pixelx2 = bmp_get_pixel(bmp_ip, i, j)==NULL?temp_pixel:bmp_get_pixel(bmp_ip, i, j);
			rgb_pixel_t *pixelx3 = bmp_get_pixel(bmp_ip, i+1, j)==NULL?temp_pixel:bmp_get_pixel(bmp_ip, i+1, j);

			//getting hold of the pixels on the top and bottom of a pixel in consideration
			rgb_pixel_t *pixely1 = bmp_get_pixel(bmp_ip, i, j-1)==NULL?temp_pixel:bmp_get_pixel(bmp_ip, i, j-1);
			rgb_pixel_t *pixely2 = bmp_get_pixel(bmp_ip, i, j)==NULL?temp_pixel:bmp_get_pixel(bmp_ip, i, j);
			rgb_pixel_t *pixely3 = bmp_get_pixel(bmp_ip, i, j+1)==NULL?temp_pixel:bmp_get_pixel(bmp_ip, i, j+1);

			//Since it is a grayscale image all red, blue and green intensities are same. So we are considering only red pixel intensities
			
			//for horizontal masking
			hred = pixelx1->red*(-1) + pixelx2->red*(0) + pixelx3->red*(1);
			
			Ih = hred;
			diffx[i][j] = Ih*Ih;

			//for vertical masking
			vred = pixely1->red*(-1) + pixely2->red*(0) + pixely3->red*(1);
			
			Iv = vred;		
			diffy[i][j] = Iv*Iv;

			//for diagonal masking
			diffxy[i][j] = Ih*Iv;

		}
	}

	//constructing the cornerness map for all the pixels
	float** cornerness;

	cornerness = (float**)malloc(sizeof(float*)*ip_width);
	for(i=0;i < ip_width; i++)
	{
	  cornerness[i] = (float*)malloc(sizeof(float)*ip_height);
	  for(j=0;j < ip_height; j++)
	  {
		if((i<ip_width-1)&&(i>0)&&(j<ip_height-1)&&(j>0))
		{
			A = convolve(diffx, gaussian_kernel, i, j);
			B = convolve(diffy, gaussian_kernel, i, j);
			C = convolve(diffxy, gaussian_kernel, i, j);
		}
		else
		{
			A = diffx[i][j];
			B = diffy[i][j];
			C = diffxy[i][j];
		}

		det_M = A*B - C*C;
		trace_M = A + B;

		cornerness[i][j] = abs(det_M - k*(trace_M*trace_M));

		if(cornerness[i][j] < threshold)
		  cornerness[i][j] = 0;
          }
	}

	//for highlighting the corners with red colour
	rgb_pixel_t corner_pixel = {0, 0, 255, 0};
	int y = r;
	int x = r;
	int maxX = ip_width-r;
	int maxY = ip_height-r;

		//determing the corner
    	for (x = r, maxX = ip_width - r; x < maxX; x++)
    	{
	
			for (y = r, maxY = ip_height - r; y < maxY; y++)
			{
				float currentValue = cornerness[x][y];
			
				for (j = -r; j <= r; j++)
				{
				
					for (i = -r; (currentValue != 0) && (i <= r); i++)
					{
						if (cornerness[x + j][y + i] > currentValue)
						{
							currentValue = 0;
							break;
						}
					}
				}	

				// check if this point is really interesting and highlighting the area around the corner
				if (currentValue != 0)
				{
					bmp_set_pixel(bmp_op, x-1, y-1, corner_pixel);
					bmp_set_pixel(bmp_op, x, y-1, corner_pixel);
					bmp_set_pixel(bmp_op, x+1, y-1, corner_pixel);
					bmp_set_pixel(bmp_op, x-1, y, corner_pixel);
					bmp_set_pixel(bmp_op, x, y, corner_pixel);
					bmp_set_pixel(bmp_op, x+1, y, corner_pixel);
					bmp_set_pixel(bmp_op, x-1, y+1, corner_pixel);
					bmp_set_pixel(bmp_op, x, y+1, corner_pixel);
					bmp_set_pixel(bmp_op, x+1, y+1, corner_pixel);
				}
			}
    	}
	//writing the final image
	bmp_save(bmp_op, "bmp_corner_seq.bmp");

	bmp_destroy(bmp_ip);
	bmp_destroy(bmp_op);

	for(i=0;i < ip_width; i++)
	{
		free(diffx[i]);
		free(diffy[i]);
		free(diffxy[i]);
		free(cornerness[i]);
	}
	free(diffx);
	free(diffy);
	free(diffxy);
	free(cornerness);
	free(temp_pixel);
	return 0;

}
