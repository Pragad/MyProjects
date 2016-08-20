#define HAPLOTYPE_LENGTH 5
#define NUMBER_OF_READS 3
#define APPEND_MODE 1
#define WRITE_MODE 0
#define ERROR_RATIO  0.4
#define NOT_SET 0
#define SET 1
#include <vector>
#include <cstring>

#pragma once
class Haplotype
{
private:	
	int readLength;
	int readStartPosition;
	int readEndPosition;
	int maxReadLength;
	char** readMartixArray;
	char** readMatrix;
	char* binaryHaplotype;
	int error_count;
	int* haplotype_bit_array;
	int* reverse_haplotype_bit_array;
	
public:	
	Haplotype(void);
	~Haplotype(void);
	int generateRandomNumber(int, int);
	void generateBinaryHaplotype(int);
	void generateReadMatrix();
	int getPositionRead(int, int);
	
	void setMaxReadLength(int, int);
	int getMaxReadLength(void);

	void setReadStartPosition(void);
	int getReadStartPosition(void);

	void setReadEndPosition(void);
	int getReadEndPosition(void);

	void setReadLength(void);
	int getReadLength(void);

	void setReadMatrixArray(void);
	char** getReadMatrixArray(void);
	void introduceErrors(void);

	void generateRead(int, int);
	void allocateReadMatrixMemory(void);
	void displayReadMatrix(void);

	int findMinimumDistance(void);
	int findErrorCount(void);

	void find_all_possible_binary_haplotype(int ,int, int, int );
	void findMinimumErrorHaplotype(void);
	void push(int );
	void pop();
	void fill_Unique_Bit_Haplotype_Array(void );
	int* array_Reverse(int*, unsigned int);
	std::string findComplementaryHaplotype(std::string);
};

