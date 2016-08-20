# coding: utf_8

import unittest
import os
import sys
import codecs
import itertools
import re
import pdb

def itemset_joinable(s1,s2):
	return all(x1 == x2 for x1,x2 in zip(s1,s2)[:-1]) and s1[-1] < s2[-1]
def itemset_join(s1,s2):
	return s1+s2[-1:]
def minus_one_subsets(s):
	for i in xrange(len(s)):
		yield s[:i]+s[i+1:]
def unique_items(L):
    found = set()
    for item in L:
#	print item
        if item not in found:
            yield item
            found.add(item)


data_file = os.path.abspath(__file__)
data_file = os.path.dirname(data_file)
#data_file = os.path.join(data_file, "best_reviews_tagged.txt")
data_file = os.path.join(data_file, "best_reviews_tagged.txt")

out_file = os.path.abspath(__file__)
out_file = os.path.dirname(out_file)
out_file = os.path.join(out_file, "best_bigrams_test.txt")

fout  = open(out_file, 'w')

part1 = []
part2 = []
noun = []
pI = []
Sent = []
SIndex = []
NIndex = []
pIndex = []
featBag = []
flag = 0
count = 0
sngC = 0
sngC2 = 0
try:
	
#	pdb.set_trace()
	fin = open(data_file, 'r')
	for line in fin:
		count += 1
		Sent.append(line)
		SIndex.append(count)
		for word in line.split(' '):
			for part in word.split('_'):
				if (flag == 0):
					pI.append(count)
					part1.append(part)
					flag = 1
				elif (flag == 1):
					part2.append(part)
					flag = 0
	fin.close()		
	count1 = 0
	count2 = 0
	nncount = 0

#	fin1 = open(data_file1, 'r')
#	for line1 in fin1:
#    	print line1
#		for word11 in line1.split(' '):
#			featBag.append(word11)
#	fin1.close()
except:
	print 'Something went wrong in Part1a.'
try:
	cnt = 0
	flag = 0
#	pdb.set_trace()
	for x, y, z in itertools.izip(part1, part2, pI):
#		print "X:",x,"Y:",y,"Z:",z,"Cnt:",cnt, "Noun:",noun
		if (y=="NN" or y=="NNS"):
			if(nncount==0):
				nncount=1
				noun.append([])
				noun[cnt].append(x)
				cnt = cnt+1
###				noun.append(x)
				#print "hi"
#				cnt += 1
#    			print "Noun1:",x
				nntemp=x
				NIndex.append(z)
			elif(nncount==1):
####				noun.pop()
				noun[cnt-1].pop()
			#	noun.pop()
#				cnt -= 1
				x=nntemp+'-'+x				
			#	noun.append([])
				cnt = cnt-1
				noun[cnt].append(x)
####				noun.append(x)
#				cnt += 1
#				print "Noun2:",x
				nncount=0
				NIndex.append(z)
		else:
				nncount=0
#	print noun
#				print "X2",x
				#flag=1
#			for t in featBag:
#				if(x==t):
#				if(flag==0):
				#elif(flag==1):
				#	Noun[count-1]=x
#				flag=0
#				break	#added at last moment as the values would be repeated
#					print "N,NI:",x,z
#	for x in noun:
#		uninoun = list(unique_items(x))
#		for nex in uninoun:
#			print nex
except:
	print 'Something went wrong in Part1b.'
result_dict = {}
min_sup = 3
freq = {}

###################################################################################### Copied from SWN
"""bb = []
temp=NIndex[0]
NewNoun = []
NewNIndex = []
uniqList = []
for n,ni in itertools.izip(noun,NIndex):
#	print "NOUN:sentence",n,ni
	if(ni==temp):
		bb.append(n)
		NewNIndex.append(ni) #added atlast while doing for small collection set
	else:
		temp=ni
		uniqList = list(unique_items(bb))
		for u in uniqList:
			NewNoun.append(u)
			NewNIndex.append(ni)
		del bb[0:len(bb)]
		bb.append(n)

print bb
uniqList = list(unique_items(bb))
for u in uniqList:
	NewNoun.append(u)
	NewNIndex.append(ni)
	"""
######################################################################################
#print "Noun:",noun
#noun1 = [['Camera-Quality','Tiger',],['Lion-Quality','Tiger'],['animal','animal','Tiger']]
#noun2 =[['camera', 'photographer', 'pictures', 'macro-size', 'coat-pocket', 'purse', 'weight', 'day', 'auto-focus', 'love'], ['scene-modes', 'situations', 'mb', 'flash', 'battery', 'unit', 'auto-mode', 'functions', 'point', 'click', 'results']]
noun3 = [['prag'],['pri'],['aaa']]
for items in noun:
	for item in set(items):
		freq[item] = freq.get(item,0)+1
		
freq_items = dict((k,v) 
	for k,v in freq.items() if v >= min_sup)
simple_trans_set = [(set(i for i in items if i in freq_items)) for items in noun]
cur_freq_item_sets = dict(((i,), f) for (i,f) in freq_items.iteritems())
result_dict[1] = cur_freq_item_sets
for cur_item_set_size in xrange(2,len(freq_items)+1):
	joined_itemsets = [itemset_join(s1,s2)
		for s1 in cur_freq_item_sets
		for s2 in cur_freq_item_sets
		if itemset_joinable(s1,s2)]
		
	pruned_itemsets = [iset for iset in joined_itemsets
		if all(sub_iset in cur_freq_item_sets
			for sub_iset in minus_one_subsets(iset)
			)
		]
	item_set_freqs = dict((itemset,0) for itemset in pruned_itemsets)

	for items in simple_trans_set:
		for itemset in pruned_itemsets:
			if all(item in items for item in itemset):
				item_set_freqs[itemset]+=1
				
	new_freq_item_sets = dict((k,v) for (k,v) in item_set_freqs.iteritems()
		if v >= min_sup)
	if len(new_freq_item_sets)==0:
		break
		
	result_dict[cur_item_set_size] = new_freq_item_sets
	cur_freq_item_sets = new_freq_item_sets
print result_dict
#pdb.set_trace()
for key,value in result_dict.items():
	v = str(value)
	word1 = " ".join(re.findall("[a-zA-Z\-]+", v))
fout.write(word1)
print word1