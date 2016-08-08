Following 2 images can be used to run the codes:
Small Image:  notredame_1024.bmp
Big Image: notredame_2048.bmp

For viewing the corners on big image, you may want to zoom in the image. Since the pixels are more and the area we are highlighting is small, corners are visible after zooming in.

For executing the sequential code use cornerDetectionSequential.c
	- for compiling
		gcc -o corner_seq bmpfile.c cornerDetectionSequential.c -lm
	
	- for executing
		./corner_seq notredame_1024.bmp

For executing the parallel code use cornerDetectionParallel.c
	- for compiling
		gcc -fopenmp -o corner_par bmpfile.c cornerDetectionParallel.c -lm
	
	- for executing
		./corner_par notredame_1024.bmp num_threads