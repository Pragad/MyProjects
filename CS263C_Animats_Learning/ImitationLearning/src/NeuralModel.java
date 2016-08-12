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


import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.AutoStepable;
import uchicago.src.sim.engine.SimpleModel;
import uchicago.src.sim.gui.DefaultGraphLayout;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Network2DDisplay;
import uchicago.src.sim.util.Random;
import uchicago.src.sim.util.RepastException;
import uchicago.src.sim.util.SimUtilities;

public class NeuralModel extends SimpleModel {
	protected Board board;
	
	//PRAG
	private int goatCount = 5;
	private int buffaloCount = 3;
	private DisplaySurface boardDisplaySurface;

	private OpenSequenceGraph officeErrorGraph;
	private OpenSequenceGraph individualGraph;
	
	//prag
	public static NeuralModel model;
	public DefaultGraphLayout layout ;
	protected int boardWidth = 500, boardHeight = 500;

	public NeuralModel() {
		// setup the random generator
		Random.createUniform();
		// and the list of agents that will eventually hold everything
		// that gets "stepped"
		if (super.agentList == null)
			super.agentList = new ArrayList();
	}

	
	public String[] getInitParam() {
		//PRAG
		return new String[] { 
				"GoatCount",
				"BuffaloCount",
				"BoardWidth", 
				"BoardHeight" 
			};
	}

	public void begin() {
		try {
			super.begin();
			// reset the agent numbers and such
			Cub.resetIndices();
			//PRAG
			Goat.resetIndices();
			Buffalo.resetIndices();
			if (super.agentList == null)
				super.agentList = new ArrayList();
			super.agentList.clear();
			board = new Board(boardWidth, boardHeight);
			board.createMother();
			board.createCub();		
			//03_11_2012
			board.addBuffalo(buffaloCount);
			board.addGoats(goatCount);
	
			//PRAG
			

			//board.createCub();
			// add them to the list of 
			super.agentList.add(board.getCub());
			super.agentList.add(board.getMother());
			
			// create all the displays
			this.buildDisplay();
			//prag add remove object code here
			//board.killAnimat(objAnimat)
			this.buildGraphs();

			// Schedule some pictures of the displays if you'd like
//			new SnapshotScheduler("display", officeDisplaySurface, "display").scheduleAtInterval(schedule, 200);
//			new SnapshotScheduler("individual", individualGraph, "individual").scheduleAtInterval(schedule, 400);
//			new SnapshotScheduler("office", officeErrorGraph, "office").scheduleAtInterval(schedule, 400);
		} catch (RepastException ex) {
			SimUtilities.showError("Error readying the model", ex);
			super.stop();
		}
	}

	/**
	 * This builds the display surface for the office 
	 */
	
	public void removeObject(Animat objAnimat)
	{
		
		layout.getNodeList().remove(objAnimat);
	}
	protected void buildDisplay() {
		//System.out.println("buildDisplay"+officeWidth+"\n");
		//Only once
		boardDisplaySurface = 
			new DisplaySurface(
					new Dimension(boardWidth, boardHeight), 
					this, 
					"Board Display");
	//	boardDisplaySurface.
		// create the graph layout that holds the agents that get displayed
		layout = new DefaultGraphLayout(boardWidth,
				boardHeight);
		layout.getNodeList().add(board.getCub());
		layout.getNodeList().add(board.getMother());
		System.out.println("Layout: "+layout);
		layout.getNodeList().add(new Prey(board));
		//PRAG
		layout.getNodeList().addAll(board.getGoats());
//03_11_2012
		layout.getNodeList().addAll(board.getBuffalos());
		
//		removeObject(board.getCub());

		// tell the display surface to display the layout (after wrapping
		// it in a Network2DDisplay
		Network2DDisplay officeNetDisplay = new Network2DDisplay(layout);
		boardDisplaySurface.addDisplayableProbeable(officeNetDisplay,
				"Office display");
		
		boardDisplaySurface.setBackground(Color.WHITE);
		boardDisplaySurface.display();
		this.registerDisplaySurface("neural", boardDisplaySurface);
	}
	
	/**
	 * This builds the error graphs. 
	 */
	protected void buildGraphs() {
		/**** The office statistics graph ****/
		officeErrorGraph = new OpenSequenceGraph("Board error statistics", this);
		// Build the error graph
		officeErrorGraph.addSequence("Avg. Lion Error", new Sequence() {
			public double getSValue() {
				double totalErr = 0;
				//prar
				for(Iterator iter = board.getLions().iterator();iter.hasNext();)
				{
					//totalErr += ((Cub) office.getCub()).getError();
					totalErr += ((Lion) iter.next()).getError();
				}
					
				//prar :
				return totalErr / board.getLions().size();
				//return totalErr; // / office.getCub().size() = 1 thts why;
			}
		});

		officeErrorGraph.addSequence("Max. Error", new Sequence() {
			public double getSValue() {
				double maxErr = Double.MIN_VALUE;
				//prar
				for(Iterator iter = board.getLions().iterator();iter.hasNext();)
				{
				//double err = ((Cub) office.getCub()).getError();
					double err = ((Lion) iter.next()).getError();
				if (err > maxErr)
					maxErr = err;
				}
				return maxErr;
			}
		});

		officeErrorGraph.addSequence("Min. Error", new Sequence() {
			public double getSValue() {
				double minErr = Double.MAX_VALUE;
				//prar
				for(Iterator iter = board.getLions().iterator();iter.hasNext();)
				{
				//double err = ((Cub) office.getCub()).getError();
					double err = ((Lion) iter.next()).getError();
					if (err < minErr)
						minErr = err;
				}
				return minErr;
			}
		});

		officeErrorGraph.setYRange(-.1, 1.1);
		officeErrorGraph.setXRange(0, 5);
		officeErrorGraph.display();
		
		/**** the individual statistics graph ****/
		individualGraph = new OpenSequenceGraph("Individual statistics", this);
		
			/*final Cub emp = (Cub)  office.getCub(); 
			individualGraph.addSequence(
					emp.getNodeLabel() + "'s error", 
					new Sequence() {
						public double getSValue() {
							return emp.getError();
						}
					},
					emp.getColor());*/
		
		for(Iterator iter = board.getLions().iterator(); iter.hasNext();)
		{
			final Lion lion = (Lion)iter.next();
			
			individualGraph.addSequence(lion.getNodeLabel() + "'s error ", 
					new Sequence() {
				public double getSValue(){
					return lion.getError();
				}
				
			//prar
				/*@Override
				public double getSValue() {
					// TODO Auto-generated method stub
					return 0;
				}*/
			},
			lion.getColor());
		}
		individualGraph.setYRange(-.1, 1.1);
		individualGraph.setXRange(0, 5);
		individualGraph.display();
	}
	
	/**
	 * Sets up the model for the next run, clears out all the old employees
	 * and the old displays
	 */
	public void setup() {
		super.setup();
		//Only once
		if (officeErrorGraph != null)
			officeErrorGraph.dispose();
		if (individualGraph != null)
			individualGraph.dispose();
		if (boardDisplaySurface != null)
			boardDisplaySurface.dispose();

		boardDisplaySurface	= null;
		
		// Add the custom action button that will randomly scatter the 
		// agents on the display surface
		getModelManipulator().addButton("Spread out agents", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					ArrayList temp = new ArrayList();
					temp.add(board.getCub());
					Cub emp = (Cub) ((ArrayList) temp.clone()).get(0); 
					emp.setX(Random.uniform.nextIntFromTo(0, board.getWidth()));
					emp.setY(Random.uniform.nextIntFromTo(0, board.getHeight()));
				
				
				boardDisplaySurface.updateDisplayDirect();
			}
		});
	}

	
	protected void preStep() {
//		System.out.println();
//		System.out.println("tick: " + getTickCount());
		try {
			for (Iterator iter = super.agentList.iterator(); iter.hasNext();) {
				((AutoStepable) iter.next()).preStep();
			}
		} catch (Exception ex) {
			SimUtilities.showError("Error preStepping the simulation", ex);
			super.stop();
		}
	}

	
	protected void step() {
		try {
			for (Iterator iter = super.agentList.iterator(); iter.hasNext();) {
				Object o = iter.next();
				((AutoStepable) o).step();
			}
		} catch (Exception ex) {
			SimUtilities.showError("Error stepping the simulation", ex);
			super.stop();
		}
	}

	protected void postStep() {
		try {
			for (Iterator iter = super.agentList.iterator(); iter.hasNext();) {
				((AutoStepable) iter.next()).postStep();
			}
		} catch (Exception ex) {
			SimUtilities.showError("Error postStepping the simulation", ex);
			super.stop();
		}
		boardDisplaySurface.updateDisplay();
		officeErrorGraph.step();
		individualGraph.step();
	}

	
	public String getName() {
		return "Neural Model";
	}

	/**
	 * @return returns the officeHeight
	 */
	public int getOfficeHeight() {
		return boardHeight;
	}

	/**
	 * @param officeHeight the officeHeight
	 */
	public void setBoardHeight(int officeHeight) {
		this.boardHeight = officeHeight;
	}

	/**
	 * @return returns the officeWidth
	 */
	public int getBoardWidth() {
		return boardWidth;
	}

	/**
	 * @param officeWidth the officeWidth
	 */
	public void setOfficeWidth(int officeWidth) {
		this.boardWidth = officeWidth;
	}
	
//PRAG
	public int getGoatCount()
	{
		return goatCount;
	}
	
	public void setGoatCount(int goatCount)
	{
		this.goatCount = goatCount;
	}
	
	public int getBuffaloCount()
	{
		return buffaloCount;
	}
	
	public void setBuffaloCount(int buffaloCount)
	{
		this.buffaloCount = buffaloCount;
	}
	
	
	
	public static void main(String[] args) {
		uchicago.src.sim.engine.SimInit init = new uchicago.src.sim.engine.SimInit();
		model = new NeuralModel();
		if (args.length > 0)
			init.loadModel(model, args[0], false);
		else
			init.loadModel(model, null, false);
	}
}