# LT-Monitor-Experiments
Loss-tolerant LTL Monitor Generator Implementation and Implementation of Experiments

This repository contains the code for the implementation of the loss-tolerant LTL monitor. The source code for experiments on MPlayer and Google-cluster-traces can be found in the package 
rithm.ltmonexperiments. 

The extracted data-set used for Google Cluster Data is at https://www.dropbox.com/s/p0kyutgl2dettnw/Google_cluster_data_extract.txt.tar.gz?dl=0 

For MPlayer experiment,the data-set is in this repository in the folder 'MPlayer_Experiment_Results'. Here, the text files contain the truth-values of the loss-tolerant monitors for Property 1 & 2.

The implementation of loss-tolerant monitor synthesis algorithm requires RV-LTL monitor as its input. We use LamaConv (http://www.isp.uni-luebeck.de/lamaconv) utility to generate RV-LTL monitors. 
The inputted RV-LTL monitors can be generated from 'LamaConv' using below command. The filename rltlconv.jar refers to the jar of LamaConv utility.

java -jar rltlconv.jar "LTL = [](a->[](b-><>c))" --props --mealy --min 

Sample output is shown below. Although the machine is described as a mealy machine, our implementation for synthesizing  loss-tolerant monitors requires every state to have a single output symbol. 
For example, q0 has its output as 'ptrue' for all possible transitions where q0 is the destination state. Similarly, q2 has its output as 'pfalse'. If this condition is not satisfied, the outout of 'LamaConv'
needs to be transformed to meet this condition in order to synthesize the corresponding loss-tolerant monitor.

MEALY {
  ALPHABET = ["(a&&c)", "(a&&b)", "(a)", "()", "(a&&b&&c)", "(b&&c)", "(c)", "(b)"]
  STATES = [q0, q1, q2]
  START = q0
  DELTA(q2, "(b)") = q2: pfalse
  DELTA(q2, "(a)") = q2: pfalse
  DELTA(q1, "()") = q1: ptrue
  DELTA(q2, "(a&&b&&c)") = q1: ptrue
  DELTA(q0, "(b)") = q0: ptrue
  DELTA(q1, "(a&&c)") = q1: ptrue
  DELTA(q0, "(a&&b&&c)") = q1: ptrue
  DELTA(q2, "(c)") = q1: ptrue
  DELTA(q1, "(c)") = q1: ptrue
  DELTA(q2, "(a&&b)") = q2: pfalse
  DELTA(q1, "(b&&c)") = q1: ptrue
  DELTA(q0, "(a)") = q1: ptrue
  DELTA(q2, "(b&&c)") = q1: ptrue
  DELTA(q1, "(a&&b&&c)") = q1: ptrue
  DELTA(q0, "(c)") = q0: ptrue
  DELTA(q1, "(a)") = q1: ptrue
  DELTA(q0, "(a&&c)") = q1: ptrue
  DELTA(q1, "(a&&b)") = q2: pfalse
  DELTA(q2, "()") = q2: pfalse
  DELTA(q0, "(a&&b)") = q2: pfalse
  DELTA(q0, "(b&&c)") = q0: ptrue
  DELTA(q1, "(b)") = q2: pfalse
  DELTA(q2, "(a&&c)") = q1: ptrue
  DELTA(q0, "()") = q0: ptrue
}

After this, the above output of 'LamaConv' is fed to our tool as per below command. 
cat <RV-LTL-Mon> | java -jar LossTolerantLTLMon.jar rithm.ltmonfactory.LTLMongenerator

The output of our tool is as shown below. It contains transitions for 'chi' symbol denoting unknown element of trace.
It also has 2 new uknown states in addition to states of RV-LTL monitor.

Initial=q0
STATE=q1,OUTPUT=q1=ptrue
STATE=q2,OUTPUT=q2=pfalse
STATE=q0,OUTPUT=q0=ptrue
ALPHABETS=["(a&&c)", "(a&&b)", "(a)", "()", "(a&&b&&c)", "(b&&c)", "(c)", "(b)"]
DELTA(q2,"(a&&c)")=q1
DELTA(q2,"(a&&b)")=q2
DELTA(q2,"(a)")=q2
DELTA(q2,"()")=q2
DELTA(q2,"(a&&b&&c)")=q1
DELTA(q2,"(b&&c)")=q1
DELTA(q2,"(c)")=q1
DELTA(q2,"(b)")=q2
DELTA(q2,chi)=X0
DELTA(q1,"(a&&c)")=q1
DELTA(q1,"(a&&b)")=q2
DELTA(q1,"(a)")=q1
DELTA(q1,"()")=q1
DELTA(q1,"(a&&b&&c)")=q1
DELTA(q1,"(b&&c)")=q1
DELTA(q1,"(c)")=q1
DELTA(q1,"(b)")=q2
DELTA(q1,chi)=X0
DELTA(q0,"(a&&c)")=q1
DELTA(q0,"(a&&b)")=q2
DELTA(q0,"(a)")=q1
DELTA(q0,"()")=q0
DELTA(q0,"(a&&b&&c)")=q1
DELTA(q0,"(b&&c)")=q0
DELTA(q0,"(c)")=q0
DELTA(q0,"(b)")=q0
DELTA(q0,chi)=X1
DELTA(X0,"(a&&c)")=q1
DELTA(X0,"(a&&b)")=q2
DELTA(X0,"(a)")=X0
DELTA(X0,"()")=X0
DELTA(X0,"(a&&b&&c)")=q1
DELTA(X0,"(b&&c)")=q1
DELTA(X0,"(c)")=q1
DELTA(X0,"(b)")=q2
DELTA(X0,chi)=X0
DELTA(X1,"(a&&c)")=q1
DELTA(X1,"(a&&b)")=q2
DELTA(X1,"(a)")=X1
DELTA(X1,"()")=X1
DELTA(X1,"(a&&b&&c)")=q1
DELTA(X1,"(b&&c)")=X1
DELTA(X1,"(c)")=X1
DELTA(X1,"(b)")=X1
DELTA(X1,chi)=X1
Total States=5,Transitions=46

We have included the example monitors generated for [](a->[](b-><>c)) and [](a-><>b) in this repository.


