#!/usr/bin/env python
import pdb
"""
Interface to SentiWordNet using the NLTK WordNet classes.

---Chris Potts
"""

import re
import os
import sys
import codecs
import itertools


data_file = os.path.abspath(__file__)
data_file = os.path.dirname(data_file)
data_file = os.path.join(data_file, "best_reviews_tagged.txt")

data_file1 = os.path.abspath(__file__)
data_file1 = os.path.dirname(data_file1)
data_file1 = os.path.join(data_file1, "best_bigrams_test.txt")

data_file2 = os.path.abspath(__file__)
data_file2 = os.path.dirname(data_file2)
data_file2 = os.path.join(data_file2, "TEST.txt")

RevT = []
noun = []
Adj = []
Index = []
part1 = []
part2 = []
flag = 0
NIndex = []
AIndex = []
Sent = []
SIndex = []
pI = []
pIndex = []
featBag = []
count = 0
ci = 0
noun1 = []
nindex1 = []
nncount = 0

def unique_items(L):
    found = set()
    for item in L:
#	print item
        if item not in found:
            yield item
            found.add(item)

try:
	fin = open(data_file, 'r')
	for line in fin:
		count += 1
		Sent.append(line)
		SIndex.append(count)
		for word in line.split(' '):
			for part in word.split('_'):
#				print part,count
				if (flag == 0):
					pI.append(count)
					part1.append(part)
					flag = 1
				elif (flag == 1):
					part2.append(part)
					flag = 0
	fin.close()
except:
	print 'Something went wrong in Part1.'
	
try:	
	print "Features are:"
	fin1 = open(data_file1, 'r')
	for line1 in fin1:
		for word1 in line1.split(' '):
			print word1
			featBag.append(word1)
	fin1.close()
	
	cntBat = 0
	cntBat2 = 0
	fout = open(data_file2,'w')
######################## from apriori17 for bigrams################################
	for x, y, z in itertools.izip(part1, part2, pI):
#		print "X:",x,"Y:",y,"Z:",z,"Cnt:",cnt, "Noun:",noun
		if (y=="NN" or y=="NNS"):
			if(nncount==0):
				nncount=1
				noun.append(x)
				NIndex.append(z)
				nntemp=x
			elif(nncount==1):
				noun.pop()
				x=nntemp+'-'+x				
				noun.append(x)
				nncount=0
		elif (y=="JJ" or y=="JJR" or y=="JJS" ):
			Adj.append(x)
			AIndex.append(z)
		else:
				nncount=0
##############################################end of apriori17##################################################
	for a,b in itertools.izip(noun, NIndex):
		for t in featBag:
			if(a==t):
				noun1.append(a)
				nindex1.append(b)
#			print "A,AI:",x,z
	fout.close()
	#print "Bat:",cntBat
except:
	print 'Something went wrong in Part2'	

#pdb.set_trace()
bb = []
temp=nindex1[0]

NewNoun = []
NewNIndex = []
uniqList = []
for n,ni in itertools.izip(noun1,nindex1):
#	print "NOUN:sentence",n,ni
	if(ni==temp):
		bb.append(n)
	else:
		temp=ni
		uniqList = list(unique_items(bb))
		for u in uniqList:
			NewNoun.append(u)
			NewNIndex.append(ni)
		del bb[0:len(bb)]
		bb.append(n)

uniqList = list(unique_items(bb))
for u in uniqList:
	NewNoun.append(u)
	NewNIndex.append(ni)

UniqNewNoun = []
UniqNewNoun = list(unique_items(NewNoun))
#print UniqNewNoun
#print "Noun:",Noun
#print "NewNoun:",NewNoun

try:
    from nltk.corpus import wordnet as wn
except ImportError:
    sys.stderr.write("Couldn't find an NLTK installation. To get it: http://www.nltk.org/.\n")
    sys.exit(2)

######################################################################

class SentiWordNet:
    def __init__(self, filename):
        """
        Argument:
        filename -- the name of the text file containing the
                    SentiWordNet database
        """        
        self.filename = filename
        self.db = {}
        self.parse_src_file()

    def parse_src_file(self):
        lines = codecs.open(self.filename, "r", "utf8").read().splitlines()
        lines = filter((lambda x : not re.search(r"^\s*#", x)), lines)
        for i, line in enumerate(lines):
            fields = re.split(r"\t+", line)
            fields = map(unicode.strip, fields)
            try:            
                pos, offset, pos_score, neg_score, synset_terms, gloss = fields
            except:
                sys.stderr.write("Line %s formatted incorrectly: %s\n" % (i, line))
            if pos and offset:
                offset = int(offset)
                self.db[(pos, offset)] = (float(pos_score), float(neg_score))

    def senti_synset(self, *vals):        
        if tuple(vals) in self.db:
            pos_score, neg_score = self.db[tuple(vals)]
            pos, offset = vals
            synset = wn._synset_from_pos_and_offset(pos, offset)
            return SentiSynset(pos_score, neg_score, synset)
        else:
            synset = wn.synset(vals[0])
            pos = synset.pos
            offset = synset.offset
            if (pos, offset) in self.db:
                pos_score, neg_score = self.db[(pos, offset)]
                return SentiSynset(pos_score, neg_score, synset)
            else:
                return None

    def senti_synsets(self, string, pos=None):
        sentis = []
        synset_list = wn.synsets(string, pos)
        for synset in synset_list:
            sentis.append(self.senti_synset(synset.name))
        sentis = filter(lambda x : x, sentis)
        return sentis

    def all_senti_synsets(self):
        for key, fields in self.db.iteritems():
            pos, offset = key
            pos_score, neg_score = fields
            synset = wn._synset_from_pos_and_offset(pos, offset)
            yield SentiSynset(pos_score, neg_score, synset)

######################################################################
            
class SentiSynset:
    def __init__(self, pos_score, neg_score, synset):
        self.pos_score = pos_score
        self.neg_score = neg_score
        self.obj_score = 1.0 - (self.pos_score + self.neg_score)
        self.synset = synset

    def __str__(self):
        """Prints just the Pos/Neg scores for now."""
        s = ""
        s += self.synset.name + "\t"
        s += "PosScore: %s\t" % self.pos_score
        s += "NegScore: %s" % self.neg_score
        return s

    def __repr__(self):
        return "Senti" + repr(self.synset)
                    
######################################################################        

if __name__ == "__main__":
    """
    If run as

    python sentiwordnet.py

    and the file is in this directory, send all of the SentiSynSet
    name, pos_score, neg_score trios to standard output.
    """
word = []
pos = []
neg = []
SWN_FILENAME = "SentiWordNet_3.0.0_20100705.txt"
if os.path.exists(SWN_FILENAME):
	swn = SentiWordNet(SWN_FILENAME)
	for senti_synset in swn.all_senti_synsets():
		word.append(senti_synset.synset.name)
		pos.append(senti_synset.pos_score)
		neg.append(senti_synset.neg_score)
		#print senti_synset.synset.name, senti_synset.pos_score, senti_synset.neg_score

#	print "WordLen",len(word)
#	print "Pos Len",len(pos)
#	print "Neg Len",len(neg)
#	pdb.set_trace()
	
	tpos = 0.0
	tneg = 0.0
	for unn in UniqNewNoun:
		for n,ni in itertools.izip(NewNoun,NewNIndex):
			if n==unn:
				for a,ai in itertools.izip(Adj,AIndex):
					if ni==ai:
						for x, y, z in itertools.izip(word, pos, neg):
							temp = x[:x.index('.')]
							if (a==temp):
								tpos = tpos + y
								tneg = tneg + z
								#print "Feature:",unn,"\t\tAdj:",a,"\t\tTPOS:",tpos,"\tTNEG:",tneg
#							print "Feature",n,"Sentiment:" ,a,"POS:",y,"NEG:",z
								break
					elif ni < ai:
						break
		print "Feature",unn,"POS:",tpos,"NEG:",tneg
		tpos = 0.0
		tneg = 0.0