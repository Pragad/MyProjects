// AllPossibleCombinations.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <iostream>
#include <vector>
#include <fstream>
#include <cstring>
#include <string>
#include <stdlib.h>
#include <algorithm>
using namespace std;


void find_all_possible_binary(int ,int, int );
void push(int );
void pop();

/******************************************************************************
* SIMPLE LIFO STACK .YOU CAN ADJUST ARRAY_SIZE ACCORDING TO YOUR SYSTEM MEMORY
replace pf with printf
*/
vector<int> stack;
vector<string> all_haplotype;
int top;

int a[4] = {2,0,2,1};
//int a[2] = {2,0};
int reva[4] = {1,2,0,2};
void push(int x){
	//stack[++top]=x;
	++top;
	stack.push_back(x);
}

void pop(){
	//stack[top--];
	--top;
	if(!stack.empty())
		stack.pop_back();
}
/******************************************************************************/

void find_all_possible_binary(int x,int temp,int flag)
{
	string strtemp ;
	ofstream filePossibleHaplotype;
	filePossibleHaplotype.open("all_possible_haplotypes.txt",ios::app);
	int i;
	if(temp!=-1)
		push(temp); 
	if(x==0)
	{
		if(filePossibleHaplotype.is_open())
		{
			for( i=0;i<=top;++i)
			{
//				strtemp.push_back(itoa(stack[i]));
				filePossibleHaplotype << stack[i];
			}
	//		all_haplotype.push_back(strtemp);
			filePossibleHaplotype << "\n";
			cout<<strtemp<<endl;
		}
		pop();
	}
	else
	{
		if(reva[x-1] == 0)
			find_all_possible_binary(x-1,0,reva[x-1]);
		else if(reva[x-1] == 1)
			find_all_possible_binary(x-1,1,reva[x-1]);
		else if(reva[x-1] == 2)
		{
			find_all_possible_binary(x-1,0,reva[x-1]);
			find_all_possible_binary(x-1,1,reva[x-1]);
		}
		pop();        
	}
	filePossibleHaplotype.close();
}


void displayAllHaplotype()
{
	for(int i =0; i<all_haplotype.size();i++)
		cout<<all_haplotype[i];
}

int sampleMatrixCompar()
{
	vector<int> errorCount;
//	char b[8][3] = {{'0','0','0'},{'0','0','1'},{'0','1','0'},{'0','1','1'},{'1','0','0'},{'1','0','1'},{'1','1','0'},{'1','1','1'}	};
	char a[3][5] = {{'1','1','9','9','9'},{'9','0','1','0','9'},{'9','9','9','1','0'}	};
//	char a[3][3] = {{'1','9','9'},{'9','1','9'},{'9','9','1'}	};
	
//	static const string all_comb_array[] = {"000","001","010","011","100","101","110","111"};
	
	
	static const string all_comb_array[] = {"00000","00001","00010","00011","00100","00101","00110","00111","01000","01001","01010","01011","01100","01101","01110","01111","10000","10001","10010","10011","10100","10101","10110","10111","11000","11001","11010","11011","11100","11101","11110","11111"};
	vector<string> str (all_comb_array, all_comb_array+sizeof(all_comb_array)/sizeof(all_comb_array[0]));

	char ** arr = new char*[str.size()];
	for(size_t i = 0; i < str.size(); i++)
	{
		arr[i] = new char[str[i].size() + 1];
		strcpy(arr[i], str[i].c_str());
		cout<<arr[i]<<"..";
	}
	cout<<endl;

	int count = 0;
	for(int x = 0; x < 32 ;x++)
	{
		for(int i=0 ;i < 3 ;i++)
		{
			for(int j=0;j < 5 ;j++)
			{	
				if(a[i][j] != '9')
				{
					if(a[i][j] != arr[x][j])
						count++;
				}
			}
		}
		errorCount.push_back(count);
		cout<<count<<"--";
		count = 0;
	}
	int xyz = min_element(errorCount.begin(), errorCount.end()) - errorCount.begin();
	cout << "min value at " << xyz<<endl<<"Value: "<<all_comb_array[xyz]<<"Error Count: "<<errorCount[xyz]<<endl;

	for(size_t i = 0; i < str.size(); i++)
	{
		delete [] arr[i];
	}
	delete [] arr;

	return count;
}


std::string findComplementaryHaplotype(std::string orginal_haplotype)
{
	string compl_haplotype = orginal_haplotype;

	for(int i =0; i<compl_haplotype.length(); i++)
	{
		if(compl_haplotype[i] == '0')
			compl_haplotype[i] = '1';
		else if(compl_haplotype[i] == '1')
			compl_haplotype[i] = '0';
	}

	cout<<"Orig: "<<orginal_haplotype<<endl<<"Comp: "<<compl_haplotype<<endl;
	return compl_haplotype ;
}


int main(){
	int no_of_bits;
	top=-1;
	cout<<"Enter number of bits: ";
	cin>>no_of_bits;
	find_all_possible_binary(no_of_bits,-1,no_of_bits);

//	int cnt = sampleMatrixCompar();
	string str = "10101109";
	string comp = findComplementaryHaplotype(str);
	cout<<endl<<comp<<endl;
	cout<<endl;
	system("PAUSE");
	return 0;
}