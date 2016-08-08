
#include <stdio.h>
#include "bmpfile.h"
#include <math.h>
#include <stdlib.h>
#include <omp.h>
#include <sys/time.h>

/*  gaussian_blur 
 *
 *  inputs:
 *      source -- source file name of file to be blurred
 *		sigma -- sigma value to blur with
 *		dst -- destination to save blurred image
 *
 *  returns pointer to a bmpfile_t structure which is the resulting image in memory
 *
 */
bmpfile_t * gaussian_blur(char* source, double sigma, char* dst, bool time);
bmpfile_t * gaussian_blur_p(char* source, double sigma, int numthreads, char* dst, bool time);

int main(int argc, char **argv) 
{
	bmpfile_t *bmp_result = NULL;
	int i, j, k;
	int width, height, depth, ksize;
	double sigma;
	double *g;
	struct timeval time1, time2;

	if (argc < 3) {
		printf("Usage: %s filename sigma.\n", argv[0]);
		return 1;
	}
	sigma = atof(argv[2]);

	//gettimeofday(&time1,0);
	//Run gaussian blur on imput file with given sigma
	bmp_result = gaussian_blur(argv[1],sigma,"bmp_blur1.bmp",1);
	//gettimeofday(&time2,0);
	//printf("1: %ld\n",((time2.tv_sec * 1000000 + time2.tv_usec)
	//	  - (time1.tv_sec * 1000000 + time1.tv_usec)));
	bmp_destroy(bmp_result);

	//gettimeofday(&time1,0);
	bmp_result = gaussian_blur_p(argv[1],sigma,2,"bmp_blur2.bmp",1);
	//gettimeofday(&time2,0);
	//printf("2: %ld\n",((time2.tv_sec * 1000000 + time2.tv_usec)
	//	  - (time1.tv_sec * 1000000 + time1.tv_usec)));
	bmp_destroy(bmp_result);

	//gettimeofday(&time1,0);
	bmp_result = gaussian_blur_p(argv[1],sigma,3,"bmp_blur2.bmp",1);
	//gettimeofday(&time2,0);
	//printf("2: %ld\n",((time2.tv_sec * 1000000 + time2.tv_usec)
	//	  - (time1.tv_sec * 1000000 + time1.tv_usec)));
	bmp_destroy(bmp_result);

	//gettimeofday(&time1,0);
	bmp_result = gaussian_blur_p(argv[1],sigma,4,"bmp_blur4.bmp",1);
	//gettimeofday(&time2,0);
	//printf("4: %ld\n",((time2.tv_sec * 1000000 + time2.tv_usec)
	//	  - (time1.tv_sec * 1000000 + time1.tv_usec)));
	bmp_destroy(bmp_result);

	//gettimeofday(&time1,0);
	bmp_result = gaussian_blur_p(argv[1],sigma,8,"bmp_blur8.bmp",1);
	//gettimeofday(&time2,0);
	//printf("8: %ld\n",((time2.tv_sec * 1000000 + time2.tv_usec)
	//	  - (time1.tv_sec * 1000000 + time1.tv_usec)));
	bmp_destroy(bmp_result);

	//gettimeofday(&time1,0);
	bmp_result = gaussian_blur_p(argv[1],sigma,16,"bmp_blur16.bmp",1);
	//gettimeofday(&time2,0);
	//printf("16: %ld\n",((time2.tv_sec * 1000000 + time2.tv_usec)
	//	  - (time1.tv_sec * 1000000 + time1.tv_usec)));
	bmp_destroy(bmp_result);


	return 0;
}


/*  gaussian_blur 
 *
 *  inputs:
 *      source -- source file name of file to be blurred
 *		sigma -- sigma value to blur with
 *		dst -- destination to save blurred image
 *		time -- computes exectution time of blur when non-zero
 *
 *  returns pointer to a bmpfile_t structure which is the resulting image in memory
 *
 */
bmpfile_t * gaussian_blur(char* source, double sigma, char* dst, bool time)
{
	bmpfile_t *bmp_test = NULL, *bmp_temp = NULL,*bmp_file = NULL;
	int width, height, depth, ksize;
	int i, j, k;
	double *g;
	struct timeval time1, time2;

	bmp_file = bmp_create_8bpp_from_file(source);


	if(time)
		gettimeofday(&time1,0);

	width = bmp_get_width(bmp_file);
	height = bmp_get_height(bmp_file);

	ksize = (int)ceil(6 * sigma);
	bmp_test = bmp_create(width, height, 8);
	bmp_temp = bmp_create(width, height, 8);


	g = malloc(sizeof(double)*ksize);

	for(i = 0; i <= ksize/2 ;i++)
	{
		g[i] =  exp(-pow(i,2)/(2*pow(sigma,2)))/sqrt(2*M_PI*pow(sigma,2));;
		//printf("%f\n",g[i]);		
	}

	if (bmp_temp != NULL) {
		int i, j;
		for (i = 0; i < width; ++i) {
			for (j = 0; j < height; ++j) {

				rgb_pixel_t *temp = malloc(sizeof(rgb_pixel_t));
				temp->blue = 0;
				temp->red  = 0;
				temp->green = 0;
				for( k = -ksize/2; k <= ksize/2; k++)
				{
					rgb_pixel_t *temp_r;
					if( i + k < 0 )
					{
						temp_r = bmp_get_pixel(bmp_file, 0, j);
					}
					else if ( i + k >= width)
					{
						temp_r = bmp_get_pixel(bmp_file, width-1, j);
					}
					else 
					{	
						temp_r = bmp_get_pixel(bmp_file, i+k, j);
					}

					uint8_t o_check;
					o_check = temp->blue;
					temp->blue += temp_r->blue*g[abs(k)];
					if(o_check > temp->blue)
					{
						temp->blue = 255;
					}
					o_check = temp->red;
					temp->red  += temp_r->red*g[abs(k)];
					if(o_check > temp->red)
					{
						temp->red = 255;
					}
					o_check = temp->green;
					temp->green += temp_r->green*g[abs(k)];

					if(o_check > temp->green)
					{
						temp->green = 255;
					}


				}
				bmp_set_pixel(bmp_temp, i, j, *temp);
			}
		}
	}

	if (bmp_test != NULL) {
		int i, j;
		for (i = 0; i < width; ++i) {
			for (j = 0; j < height; ++j) {

				rgb_pixel_t *temp = malloc(sizeof(rgb_pixel_t));
				temp->blue = 0;
				temp->red  = 0;
				temp->green = 0;
				for( k = -ksize/2; k <= ksize/2; k++)
				{
					rgb_pixel_t *temp_r;
					if( j + k < 0)
					{
						temp_r = bmp_get_pixel(bmp_temp, i, 0);
					}
					else if (j + k >= height)
					{
						temp_r = bmp_get_pixel(bmp_temp, i, height-1);
					}
					else 
					{	
						temp_r = bmp_get_pixel(bmp_temp, i, j+k);
					}
					uint8_t o_check;
					o_check = temp->blue;
					temp->blue += temp_r->blue*g[abs(k)];
					if(o_check > temp->blue)
					{
						temp->blue = 255;
					}
					o_check = temp->red;
					temp->red  += temp_r->red*g[abs(k)];
					if(o_check > temp->red)
					{
						temp->red = 255;
					}
					o_check = temp->green;
					temp->green += temp_r->green*g[abs(k)];

					if(o_check > temp->green)
					{
						temp->green = 255;
					}
				}
				bmp_set_pixel(bmp_test, i, j, *temp);
			}
		}
		if(time)
			gettimeofday(&time2,0);
		bmp_save(bmp_test, dst);
	}

	bmp_destroy(bmp_file);

	if(time)
		printf("1: %ld\n",((time2.tv_sec * 1000000 + time2.tv_usec)
			  - (time1.tv_sec * 1000000 + time1.tv_usec)));

	return bmp_test;

}


/*  gaussian_blur_p 
 *
 *  inputs:
 *      source -- source file name of file to be blurred
 *		sigma -- sigma value to blur with
 *		numthreads -- number of thread to compute with
 *		dst -- destination to save blurred image
 *		time -- computes exectution time of blur when non-zero
 *
 *  returns pointer to a bmpfile_t structure which is the resulting image in memory
 *
 */
bmpfile_t * gaussian_blur_p(char* source, double sigma, int numthreads, char* dst, bool time)
{
	bmpfile_t *bmp_test = NULL, *bmp_temp = NULL,*bmp_file = NULL;
	int width, height, depth, ksize;
	int i, j, k;
	double *g;
	struct timeval time1, time2;
	double t1, t2;
	rgb_pixel_t *temp_r, *temp;
	omp_set_num_threads(numthreads);

	bmp_file = bmp_create_8bpp_from_file(source);

	if(time)
	{
		t1 = omp_get_wtime();
		gettimeofday(&time1,0);
	}
	width = bmp_get_width(bmp_file);
	height = bmp_get_height(bmp_file);

	ksize = (int)ceil(6 * sigma);
	bmp_test = bmp_create(width, height, 8);
	bmp_temp = bmp_create(width, height, 8);


	g = malloc(sizeof(double)*ksize);

	for(i = 0; i <= ksize/2 ;i++)
	{
		g[i] =  exp(-pow(i,2)/(2*pow(sigma,2)))/sqrt(2*M_PI*pow(sigma,2));;
		//printf("%f\n",g[i]);		
	}
	
#pragma omp parallel private(i,j,temp_r,temp,k) shared(bmp_temp,bmp_test)
	{
		if (bmp_temp != NULL) {
			temp = malloc(sizeof(rgb_pixel_t));
			#pragma omp for schedule(dynamic, 1) nowait 
			for (i = 0; i < width; ++i) {
				for (j = 0; j < height; ++j) {

					temp->blue = 0;
					temp->red  = 0;
					temp->green = 0;
					for( k = -ksize/2; k <= ksize/2; k++)
					{

						if( i + k < 0 )
						{
							temp_r = bmp_get_pixel(bmp_file, 0, j);
						}
						else if ( i + k >= width)
						{
							temp_r = bmp_get_pixel(bmp_file, width-1, j);
						}
						else 
						{	
							temp_r = bmp_get_pixel(bmp_file, i+k, j);
						}

						uint8_t o_check;
						o_check = temp->blue;
						temp->blue += temp_r->blue*g[abs(k)];
						if(o_check > temp->blue)
						{
							temp->blue = 255;
						}
						o_check = temp->red;
						temp->red  += temp_r->red*g[abs(k)];
						if(o_check > temp->red)
						{
							temp->red = 255;
						}
						o_check = temp->green;
						temp->green += temp_r->green*g[abs(k)];

						if(o_check > temp->green)
						{
							temp->green = 255;
						}
					}
					bmp_set_pixel(bmp_temp, i, j, *temp);
				}
			}
		}


	#pragma omp barrier

		if (bmp_test != NULL) {
			temp = malloc(sizeof(rgb_pixel_t));
		#pragma omp for schedule(dynamic, 1) nowait 
			for (i = 0; i < width; ++i) {
				for (j = 0; j < height; ++j) {
					temp->blue = 0;
					temp->red  = 0;
					temp->green = 0;
					for( k = -ksize/2; k <= ksize/2; k++)
					{

						if( j + k < 0)
						{
							temp_r = bmp_get_pixel(bmp_temp, i, 0);
						}
						else if (j + k >= height)
						{
							temp_r = bmp_get_pixel(bmp_temp, i, height-1);
						}
						else 
						{	
							temp_r = bmp_get_pixel(bmp_temp, i, j+k);
						}
						uint8_t o_check;
						o_check = temp->blue;
						temp->blue += temp_r->blue*g[abs(k)];
						if(o_check > temp->blue)
						{
							temp->blue = 255;
						}
						o_check = temp->red;
						temp->red  += temp_r->red*g[abs(k)];
						if(o_check > temp->red)
						{
							temp->red = 255;
						}
						o_check = temp->green;
						temp->green += temp_r->green*g[abs(k)];

						if(o_check > temp->green)
						{
							temp->green = 255;
						}
					}
					bmp_set_pixel(bmp_test, i, j, *temp);
				}
			}

		}
	}
		if(time)
		{
			t2 = omp_get_wtime();
			gettimeofday(&time2,0);
		}
		bmp_save(bmp_test, dst);

	bmp_destroy(bmp_file);

	if(time)
	{
		printf("%d: %f\n",numthreads,t2-t1);
		printf("%d: %ld\n",numthreads,((time2.tv_sec * 1000000 + time2.tv_usec)
			  - (time1.tv_sec * 1000000 + time1.tv_usec)));
	}

	return bmp_test;

}




