#include "StdAfx.h"
#include "Haplotype.h"
#include <ctime>
#include <algorithm>
#include <fstream>
#include <iostream>
#include <cmath>
#include <vector>
#include <string>
#include <cstring>
#include <stdlib.h>
#include <algorithm>
#include <sstream>
using namespace std;

vector<int> stack;
int top=-1;
vector<string> allPossibleHaplotypes;
vector<string> allPossibleComplHaplotypes;


Haplotype::Haplotype(void)
{
	cout<<"Constructor"<<endl;
}


Haplotype::~Haplotype(void)
{
	cout<<"Destructor"<<endl;
/*	for(int i = 0; i < NUMBER_OF_READS; i++)
		delete [] readMatrix[i];
	delete [] readMatrix;
	*/
}

void Haplotype::setMaxReadLength(int haplotypeLength, int numReads)
{
	maxReadLength = haplotypeLength/numReads*2;
}

int Haplotype::getMaxReadLength()
{
	return maxReadLength;
}

void Haplotype::setReadStartPosition()
{
	readStartPosition = getPositionRead(0, HAPLOTYPE_LENGTH-1);
}

int Haplotype::getReadStartPosition()
{
	return readStartPosition;
}

void Haplotype::setReadEndPosition()
{
	readEndPosition = getPositionRead(readStartPosition, HAPLOTYPE_LENGTH);
}

int Haplotype::getReadEndPosition()
{
	return readEndPosition;
}

void Haplotype::setReadLength()
{
	readLength = readEndPosition - readStartPosition+1;
}

int Haplotype::getReadLength()
{
	return readLength;
}

void Haplotype::generateBinaryHaplotype(int haplotype_length)
{
	binaryHaplotype = new char[haplotype_length];
	/*
	int random_integer, lowest = 0, highest = 1, range;
	cout<<"Generate Binary Haplotype"<<endl;
	ofstream fileBinayHaplotype;
	fileBinayHaplotype.open("haplotype_binary.txt");
	
	//Generate Random Number
	srand((unsigned)time(0)); 
	range=(highest-lowest)+1; 
	for(int i =0;i<haplotype_length;i++)
	{
		random_integer = lowest+int(range*rand()/(RAND_MAX + 1.0));
		fileBinayHaplotype << random_integer; 
		binaryHaplotype[i] = '0' + random_integer;
	}
	
	fileBinayHaplotype.close();
	*/
	binaryHaplotype[0] = '1';
	binaryHaplotype[1] = '0';
	binaryHaplotype[2] = '1';
	binaryHaplotype[3] = '1';
	binaryHaplotype[4] = '0';
}

int  Haplotype::getPositionRead(int lowest, int highest)
{
	int random_integer1, range1;
	//Read Starting Position
	range1 = (highest-lowest)+1; 
	random_integer1 = lowest+int(range1*rand()/(RAND_MAX + 1.0));
	return random_integer1;
}

void Haplotype::allocateReadMatrixMemory()
{
	readMatrix = new char*[NUMBER_OF_READS];
	for(int i =0;i<NUMBER_OF_READS;i++)
		readMatrix[i] = new char[HAPLOTYPE_LENGTH];
}
void Haplotype::generateRead(int readCount, int file_mode)
{
	int count = 0;
	char temp;
	
	setMaxReadLength(HAPLOTYPE_LENGTH, NUMBER_OF_READS);
	setReadStartPosition();
	do
	{
		setReadEndPosition();
	}while((getReadEndPosition() - getReadStartPosition()) > getMaxReadLength());
	setReadLength();
	
	ifstream fileBinayHaplotype;
	fileBinayHaplotype.open("haplotype_binary.txt");
	ofstream fileReadMatrix;
	if(file_mode == APPEND_MODE)
		fileReadMatrix.open("read_matrix.txt", ios::app);
	else
		fileReadMatrix.open("read_matrix.txt");

//	cout<<"Read Start: "<<getReadStartPosition()<<"; Read End: "<<getReadEndPosition()<<"; Len: "<<getReadLength()<<endl;
	
	if (fileBinayHaplotype.is_open() && fileReadMatrix.is_open()) 
	{
		while (fileBinayHaplotype >> temp) 
		{
			if(count >= getReadStartPosition() && count <= getReadEndPosition() )
			{
				readMatrix[readCount][count] = temp;
				fileReadMatrix << temp;
			}
			else
			{
				readMatrix[readCount][count] = '9';
				fileReadMatrix << readMatrix[readCount][count];
			}
			count++;
		 }
		fileReadMatrix<< "\n";
	}
	fileReadMatrix.close();
	fileBinayHaplotype.close();
}

void Haplotype::generateReadMatrix()
{
	setReadMatrixArray();
	generateBinaryHaplotype(HAPLOTYPE_LENGTH);
	allocateReadMatrixMemory();
	if(NUMBER_OF_READS > 0)
		generateRead(0,WRITE_MODE);
	for(int i = 1; i< NUMBER_OF_READS; i++)
		generateRead(i,APPEND_MODE);
	displayReadMatrix();
//	introduceErrors();
	cout<<endl;
//	displayReadMatrix();
}

void Haplotype:: setReadMatrixArray()
{
	/*char tempReadMartixArray[NUMBER_OF_READS][HAPLOTYPE_LENGTH] = {
	{'9','9','9','9','9','9','9','9','9','9','9','9','1','1','9'},
	{'9','0','1','0','1','9','9','9','9','9','9','9','9','9','9'},
	{'9','9','9','9','9','9','9','1','1','1','0','9','9','9','9'},
	{'9','9','9','9','9','9','9','9','9','9','0','1','1','1','1'},
	{'9','9','9','9','9','9','9','9','9','9','9','1','1','1','1'}};
	*/
	//HardCoding
	char tempReadMartixArray[NUMBER_OF_READS][HAPLOTYPE_LENGTH] = {
	{'1','1','9','9','9'},
	{'9','9','9','1','0'},
	{'9','0','1','0','9'}};
	
	//char tempReadMartixArray[3][3] = {{'1','9','9'},{'9','0','9'},{'9','9','1'}	};

	readMartixArray = new char*[NUMBER_OF_READS];
	for(int i=0;i<NUMBER_OF_READS;i++)
	{
		readMartixArray[i] = new char[HAPLOTYPE_LENGTH];
		for(int j =0; j<HAPLOTYPE_LENGTH; j++)
		{
			readMartixArray[i][j] = tempReadMartixArray[i][j];
		}
	}
}

char** Haplotype:: getReadMatrixArray()
{
	return readMartixArray;
}

void Haplotype::displayReadMatrix()
{
	
	for(int i =0; i<NUMBER_OF_READS; i++)
	{
		for(int j =0; j<HAPLOTYPE_LENGTH; j++)
		{
			cout<<readMartixArray[i][j]<<" ";
			//cout<<readMartixArray[i][j]<<" ";
		}
		cout<<endl;
	}
}

void Haplotype::introduceErrors()
{
	int num_Errors = ERROR_RATIO * NUMBER_OF_READS;
	cout<<"Errors:"<<num_Errors<<endl;
	for(int x = 0; x<num_Errors; )
	{
		int rand_i = int((NUMBER_OF_READS)*rand()/(RAND_MAX + 1.0));
		int rand_j = int((HAPLOTYPE_LENGTH)*rand()/(RAND_MAX + 1.0));

		if(readMartixArray[rand_i][rand_j] == '1' )
		{
			readMartixArray[rand_i][rand_j] = '0';
			x++;
		}
		else if(readMartixArray[rand_i][rand_j] == '0' )
		{
			readMartixArray[rand_i][rand_j] = '1';
			x++;
		}
		else
			continue;
	}
}



void Haplotype::push(int x)
{
	++top;
	stack.push_back(x);
}

void Haplotype::pop()
{
	--top;
	if(!stack.empty())
		stack.pop_back();
}
int* Haplotype::array_Reverse(int* orig, unsigned int b)
{
	unsigned int a=0;
	int temp;
	for(a;a<--b;a++)
	{
		temp = orig[a];
		orig[a] = orig[b];
		orig[b] = temp;
	}
	return orig;
}

void Haplotype::fill_Unique_Bit_Haplotype_Array()
{
	int zeroFlag = NOT_SET;
	int oneFlag = NOT_SET;
	haplotype_bit_array = new int(HAPLOTYPE_LENGTH);
	for(int i =0; i<HAPLOTYPE_LENGTH; i++)
	{
		for(int j =0; j<NUMBER_OF_READS; j++)
		{
			if(readMartixArray[j][i] == '0')
				zeroFlag = SET;
			else if (readMartixArray[j][i] == '1')
				oneFlag = SET;
		}
		if(zeroFlag==SET && oneFlag==SET)
			haplotype_bit_array[i] = SET + SET;
		else if(zeroFlag==SET && oneFlag==NOT_SET)
			haplotype_bit_array[i] = NOT_SET;
		else if(zeroFlag==NOT_SET && oneFlag==SET)
			haplotype_bit_array[i] = SET;
		zeroFlag = NOT_SET;
		oneFlag = NOT_SET;
	}
	reverse_haplotype_bit_array = array_Reverse(haplotype_bit_array, HAPLOTYPE_LENGTH);
	
	for(int i =0;i<HAPLOTYPE_LENGTH;i++)
	{
	//	cout<<reverse_haplotype_bit_array[i]<<"--";
		cout<<reverse_haplotype_bit_array[i]<<"..";
	}

}


void Haplotype::find_all_possible_binary_haplotype(int x,int temp, int file_mode, int no_of_bits)
{
	ofstream filePossibleHaplotype;
	if(file_mode == APPEND_MODE)
		filePossibleHaplotype.open("all_possible_haplotypes.txt",ios::app);
	else
		filePossibleHaplotype.open("all_possible_haplotypes.txt");
	
	int i;
	if(temp!=-1)
		push(temp); 
	if(x==0)
	{
		if(filePossibleHaplotype.is_open())
		{
			for( i=0;i<=top;++i)
				filePossibleHaplotype << stack[i];
			filePossibleHaplotype << "\n";
		}
		pop();
	}
	else
	{   
		if(reverse_haplotype_bit_array[x-1] == 0)
			find_all_possible_binary_haplotype(x-1,0,APPEND_MODE,reverse_haplotype_bit_array[x-1]);
		else if(reverse_haplotype_bit_array[x-1] == 1)
			find_all_possible_binary_haplotype(x-1,1,APPEND_MODE,reverse_haplotype_bit_array[x-1]);
		else if(reverse_haplotype_bit_array[x-1] == 2)
		{
			find_all_possible_binary_haplotype(x-1,0, APPEND_MODE,reverse_haplotype_bit_array[x-1]);
			find_all_possible_binary_haplotype(x-1,1, APPEND_MODE,reverse_haplotype_bit_array[x-1]);
		}
		pop();        
	}
	filePossibleHaplotype.close();
}

std::string Haplotype::findComplementaryHaplotype(std::string orginal_haplotype)
{
	std::string compl_haplotype = orginal_haplotype;

	for(int i =0; i<compl_haplotype.length(); i++)
	{
		if(compl_haplotype[i] == '0')
			compl_haplotype[i] = '1';
		else if(compl_haplotype[i] == '1')
			compl_haplotype[i] = '0';
	}
	return compl_haplotype ;
}

void Haplotype::findMinimumErrorHaplotype()
{
	std::string line_haplotype;
	ifstream fileReadMatrix;
	fileReadMatrix.open("all_possible_haplotypes.txt");
	if(fileReadMatrix.is_open())
	{	
		while (getline(fileReadMatrix, line_haplotype))
		{
			allPossibleHaplotypes.push_back(line_haplotype);
			allPossibleComplHaplotypes.push_back(findComplementaryHaplotype(line_haplotype));
		}

//////////////////////////////////////////////////////

	char ** allPossibleHapArray = new char*[allPossibleHaplotypes.size()];
	char ** allPossibleCompHapArray = new char*[allPossibleComplHaplotypes.size()];
	vector<int> errorCount;
	vector<int> errorCountComp;

	for(size_t i = 0; i < allPossibleHaplotypes.size(); i++)
	{
		allPossibleHapArray[i] = new char[allPossibleHaplotypes[i].size() + 1];
		strcpy(allPossibleHapArray[i], allPossibleHaplotypes[i].c_str());

		allPossibleCompHapArray[i] = new char[allPossibleHaplotypes[i].size() + 1];
		strcpy(allPossibleCompHapArray[i], allPossibleComplHaplotypes[i].c_str());
	}

	int count = 0;
	int countComp = 0;
	
	for(int x = 0; x < allPossibleHaplotypes.size() ;x++)
	{
		for(int i=0 ;i < NUMBER_OF_READS  ;i++)
		{
			for(int j=0;j < HAPLOTYPE_LENGTH  ;j++)
			{	
				if(readMartixArray[i][j] != '9')
				{
					if(readMartixArray[i][j] != allPossibleHapArray[x][j])
						count++;
					if(readMartixArray[i][j] != allPossibleCompHapArray[x][j])
						countComp++;
				}
			}
		}
		if(count <= countComp)
		{
			errorCount.push_back(count);
			errorCountComp.push_back(INT_MAX);
		}
		else
		{
			errorCount.push_back(INT_MAX);
			errorCountComp.push_back(countComp);
		}
		count = 0;
		countComp = 0;
	}
	int xyz = min_element(errorCount.begin(), errorCount.end()) - errorCount.begin();
	cout <<endl<< "Minimum Error Haplotype: " << allPossibleHapArray[xyz]<<"; Found at: "<<xyz<<endl<<"Error Count: "<<errorCount[xyz]<<endl;
	cout << "Complementary Haplotype: "<< allPossibleCompHapArray[xyz]<<endl;
	for(size_t i = 0; i < allPossibleHaplotypes.size(); i++)
	{
		delete [] allPossibleHapArray[i];
	}
	delete [] allPossibleHapArray;
/////////////////////////////////////////////////////

	}
}

int Haplotype::findErrorCount()
{
	error_count = 0;
	for(int i =0; i<NUMBER_OF_READS; i++)
	{
		for(int j =0; j<HAPLOTYPE_LENGTH; j++)
		{
			if(readMartixArray[i][j] != binaryHaplotype[j])
				error_count++;
		}
	}
	return error_count;
}