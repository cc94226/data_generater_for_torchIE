# datagenerater_for_torchIE

These programs generate the data used for torchIE project's bioinfer branch (https://github.com/qulizhen/torchIE).

The main program is BioJsonProcesser.java. Arguments for this program are:
	inputfile -- args[0] input json data file
	output -- args[1]
	entityListFile -- args[2];
	dependencyFile -- args[3];
	postagFile -- args[4];
	lseq -- args[5];
	rseq -- args[6];
	text_toks -- args[7];
	lPos -- args[8];
	rPos -- args[9];

Please edit the arguments in Eclipse, Run -> Run configration -> Select the BioJsonProcesser on the lsft file browser -> edit the arguments in "Arguments" tab on the right.

Addtional part:

to generate the data can be used for torchIE, please follow these steps:

1. ReBioinfer dataset to xmi files: 
Org.data61.bioinfer: 
BioinferReaqder 
BioRERunner 
 ./scala_run org.data61.bioinfer.BioRERunner 
 
2. Xmi to json: 
org.nicta.transferLearningTest.CreateJsonObject.scala change input xmi dir 
Org.nicta.ie.relationExtraction.uima.ae.instCreator.JsonInstanceCreator.java  ->  to change output path 
 
3. xmi to embedding: 
Org.nicta.transferLearningTest.Bioinferembedder.scala 
./scala_run org.nicta.transferLearningTest.Bioinferembedder /home/chengchen/BIoinfer/xmi_result /media/data2tb4/wordEmbedings /home/chengchen/BIoinfer/embed_result 
Add the embedding number (can be count by wc –l "fileyouget from last step") and dim number(i.e. 200) to the embedding file you got from last strp. 
Use gensim(https://radimrehurek.com/gensim/models/word2vec.html) and word@vec.py modified by Zhuang Li on (izhuang@CRL-DLGPU:/media/data2tb1/SKIPGRAM_EMBEDDING // GPU 1 or 2 in Nicta) to convert the embedding file to binary file 
 
In Python terminal: 
model = gensim.models.Word2vec.load_word2Vec_format('/home/chengchen/BIoinfer/embed_result',binary=False) 
 
model.save_word2vec_format('/home/chengchen/BIoinfer/embed_gensim',None,True) 
 
Then use this https://github.com/rotmanmi/word2vec.torch/blob/master/bintot7.lua to convert binary embedding file to torch7 friendly file (save as *.th)

QUestion please send to cc94226@live.com
