/*$$
 * Copyright (c) 2004, Repast Organization for Architecture and Design (ROAD)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with 
 * or without modification, are permitted provided that the following 
 * conditions are met:
 *
 *	 Redistributions of source code must retain the above copyright notice,
 *	 this list of conditions and the following disclaimer.
 *
 *	 Redistributions in binary form must reproduce the above copyright notice,
 *	 this list of conditions and the following disclaimer in the documentation
 *	 and/or other materials provided with the distribution.
 *
 * Neither the name of the ROAD nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE TRUSTEES OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *$$*/


import org.joone.engine.Monitor;

import uchicago.src.sim.adaptation.neural.NeuralException;
import uchicago.src.sim.util.RepastException;
import uchicago.src.sim.util.SimUtilities;

public class Cub extends Lion {
	
	
	public static final int Lioness = 0;
	public static int age = 0;
	
	
	private static boolean self = false;
	

	private boolean wasScolded = false;	
	private double angst = 1;

	
	public Cub(double x, double y, Board board) throws RepastException {
		
		super(x, y, board, Constant.CUB, "cub.jpg", Constant.smallSize);

		super.buildNeuralNetwork();
	}

	@Override
	public void preStep() throws NeuralException {
		
		if(self){
		// train the network based on the previous step
			super.train();
		}

		// reset for this step
		this.wasScolded = false;
	}
	
	@Override
	//PRAR 03/17/2012
	public void step() throws NeuralException{
		/*if(this.age > 500)
			self = true;*/
		if(self){
			super.step();
		}else{
			doHunting();
		}
	}
	
	@Override
	public void postStep() throws Exception{
		this.age = (int)NeuralModel.model.getTickCount();
		if(self){
			super.postStep();
		}
		//prar
		else{
			Mother mother = (Mother) board.getMother();
			double x = mother.getX();
			double y = mother.getY();
			// TODO implement RangeVisibility
			//if(getRangeVisibilty() == 0){
				this.setX(x - 10);
				this.setY(y - 10);
			//}
		}	
	}
	
	public int getAge()
	{
		return this.age;
	}

	/**
	 * This trains the network based on the previous steps actions and what the
	 * network should've done the previous step
	 * 
	 * @throws NeuralException
	 *             when there is an error training the network
	 */
	public synchronized void train() throws NeuralException {
		int actionShouldveBeen;

		// figure out what action the network should've performed, so we can
		// train it to perform that correct action. Note, while the network
		// is supposed to be learning XOR, we don't have to put that logic into
		// this agent. The agent just knows whether or not it was scolded in
		// the last step. If the agent wasn't scolded then it will train the
		// network to produce the same behavior again; if it was scolded it will
		// train the network to perform a different action.
		if (wasScolded) {
			if (super.actionPerformedInStep == DONT_HUNT)
				actionShouldveBeen = DO_HUNT;
			else
				actionShouldveBeen = DONT_HUNT;
			// System.out.println("was scolded did:" +
			// this.actionPerformedInStep + ", shoulda:"
			// + actionShouldveBeen);
		} else {
			actionShouldveBeen = this.actionPerformedInStep;
		}
		this.net.getNet().getMonitor().setLearningRate(.8);
		this.net.getNet().getMonitor().setMomentum(0.3);

		// compute the network's error
		super.error = Math.abs(actionShouldveBeen - super.retrievedValue);
		// System.out.println("Error: " + error);

		// get the object that watches over the training
		Monitor monitor = net.getNet().getMonitor();

		// set the monitor parameters
		monitor.setTrainingPatterns(1);
		monitor.setTotCicles(1);

		// setup the inputs for the next round of training
		this.desiredNetworkOutput
				.setInputArray(new double[][] { { actionShouldveBeen } });
		
		//this.inputForTraining
			//		.setInputArray(new double[][] { super.getDouble(normalizedCordinates()), super.getDouble(normalizedSize()), super.getDouble(getPos()), super.getDouble(getEnergy()) });
		
		this.inputForTraining.setInputArray( properInfut() );
		// now actually train the network
		try {
			this.net.train(inputForTraining);
		} catch (NeuralException ex) {
			SimUtilities.showError("Error training neural network for agent \""
					+ getNodeLabel() + "\"", ex);
			throw ex;
		}
	}
	
	public void scold() {
		this.angst += .1;
		this.wasScolded = true;
	}

	public void praise() {
		this.angst -= .05;
	}

}