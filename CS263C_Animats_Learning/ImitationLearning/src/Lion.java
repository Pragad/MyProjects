import java.awt.Image;
import java.util.ArrayList;
import java.util.Iterator;

import org.joone.engine.DirectSynapse;
import org.joone.engine.FullSynapse;
import org.joone.engine.LinearLayer;
import org.joone.engine.Monitor;
import org.joone.engine.Pattern;
import org.joone.engine.SigmoidLayer;
import org.joone.io.MemoryInputSynapse;

import uchicago.src.sim.adaptation.neural.NeuralException;
import uchicago.src.sim.adaptation.neural.NeuralUtils;
import uchicago.src.sim.adaptation.neural.RepastNeuralWrapper;
import uchicago.src.sim.engine.AutoStepable;
import uchicago.src.sim.util.Random;
import uchicago.src.sim.util.RepastException;
import uchicago.src.sim.util.SimUtilities;

public class Lion extends Animat implements AutoStepable {


	protected double error = 0.0;

	protected Board board;
	private static final int CLOSER = 0;
	private static final int FARTHER = 1;

	protected double retrievedValue = 0.0;

	public static final int DONT_HUNT = 0;

	public static final int DO_HUNT = 1;

	public static int width;
	public static int height;

	private Image lionPicture;
	int energy =0;
	/**
	 * The action the network said to perform
	 */
	protected int actionPerformedInStep = DONT_HUNT;

	protected double[] prevStepCommands =
			new double[] { 0, 0, 0 ,0 };

	/**
	 * This is the neural network used by this lion
	 */
	protected RepastNeuralWrapper net;

	/**
	 * This is used to store the output that we want to train the network
	 * towards
	 */
	protected MemoryInputSynapse desiredNetworkOutput;
	/**
	 * This is used to store the input that results in the desired output
	 */
	protected MemoryInputSynapse inputForTraining;

	/**
	 * This is the input synapse used to grab information from the network.
	 * DirectSynapses work the best for this type of usage, that is, when a
	 * single input pattern will be applied to the network.
	 */
	protected DirectSynapse inputForRetrieval;
	private ArrayList<Animat> alRange = new ArrayList<Animat>();

	public Lion(double x, double y, Board board, int type, String image, int size) throws RepastException {

		super(type, x, y, image, size);

		this.board = board;
	}


	/**
	 * The action the network said to perform
	 */

	public void buildNeuralNetwork() throws RepastException {
		/** And this is how it is built through Repast **/
		this.net = NeuralUtils.buildNetwork(new int[] { 4, 8, 1 }, new Class[] {
				LinearLayer.class, SigmoidLayer.class, SigmoidLayer.class },
				new Class[] { FullSynapse.class, FullSynapse.class });

		// now we specify the learning parameters
		this.net.getNet().getMonitor().setLearningRate(.8);
		this.net.getNet().getMonitor().setMomentum(0.3);

		this.inputForTraining = new MemoryInputSynapse();
		// set the synapse to have two columns of input corresponding
		// to the number of rows in the input layer
		this.inputForTraining.setAdvancedColumnSelector("1,2,3,4");

		// this will hold the data we wish the network produced
		this.desiredNetworkOutput = new MemoryInputSynapse();
		// set the synapse to have one columns of output corresponding
		// to the number of rows in the output layer
		this.desiredNetworkOutput.setAdvancedColumnSelector("1");

		/** set up the rest of the network info **/
		this.inputForRetrieval = new DirectSynapse();
		this.inputForRetrieval.setName("RetrievingInput MemoryInputSynapse");

		// set the stream that the teacher will look to for computing
		// errors and teaching the network
		this.net.getNet().getTeacher().setDesired(desiredNetworkOutput);
		System.out.println("Build neural net done");
	}

	public void preStep() throws NeuralException {
		System.out.println("Called once :(");
		//Not called once also??
		//getEnergy();
		// train the network based on the previous step
		train(); //Local function written for the cub
	}

	public void step() throws NeuralException {
		try {
			prevStepCommands = getInputs();


			this.retrievedValue = retrieve();

			//			System.out.println("retrievedValue: " + retrievedValue);

			// act based on what the network said to do
			if (Math.round(retrievedValue) == DONT_HUNT) {
				dontHunt();
			} else {
				doHunting();
			}

		} catch (NeuralException ex) {
			SimUtilities.showError(
					"Error computing the next action to perform. \n"
							+ "Agent \"" + getNodeLabel() + "\".", ex);
			throw ex;
		}
	}

	protected void dontHunt() {
		actionPerformedInStep = DONT_HUNT;
	}

	protected void doHunting() {
		actionPerformedInStep = DO_HUNT;
	}


	/**
	 * This method retrieves from the network the best action to perform based
	 * on the boss commands.
	 * 
	 * @return the value the network returns
	 * 
	 * @throws NeuralException when there is an error querying the network
	 */
	private synchronized double retrieve() throws NeuralException {
		// setup the network input for the current state

		inputForRetrieval.fwdPut(new Pattern(getInputs()));

		// query the network
		Pattern retrievedPattern = net.retrieve(inputForRetrieval);
		double output = retrievedPattern.getValues()[0];
		//System.out.println("o/p"+output+"\n");
		//Caleed every run
		return output;
	}


	@Override
	public void postStep() throws Exception {
		//System.out.println("lions"+board.getLions());
		//prar
		//for to be commented
		//	for (Iterator iter = board.getLions().iterator(); iter.hasNext();) {
		//		Lion lion = (Lion) iter.next();
		Lion lion = board.getMother();
		double actionPerformed = lion.getActionPerformed();

		// System.out.println("commands: " + simplifiedCommands[0] + ","
		// + simplifiedCommands[1]);

		// apply the xor logic
		int correctAction = doHunt();
		// System.out.println("correctAction: " + correctAction + ", did: "
		// + Math.round(actionPerformed));

		board.movePreys();
	//03_11_2012
		if (correctAction == Math.round(actionPerformed)) {
			// System.out.println("praised");
			moveLion(lion, CLOSER);
		} else {
			// System.out.println("scolded");
			moveLion(lion, FARTHER);
		}

		//	}
	}

	/**
	 * this handles the moving of the agents
	 * 
	 * @param lion
	 *            the cubloyee to move
	 * @param direction
	 *            whether to move the cubloyee towards the bosses (the center of
	 *            the display) or away
	 */
	private void moveLion(Lion lion, int direction) {
		int xSide, ySide;
		this.energy -= 25;
		Board objBoard = new Board(500,500);
		//	System.out.println("Lx1: "+lion.getX()+"Ly1: "+lion.getY());
		objBoard.moveFarther(lion);
		//	System.out.println("Lx2: "+lion.getX()+"Ly2: "+lion.getY());
		// this algorithm says quadrants are like so (xSide, ySide)
		/*
		 *  | 
		 *(2,0) (1,0) (0,0) 
		 *			|
		 * -------(1,1)--------- 
		 * 			| 
		 * (2,2) (1,2) (0,2) 
		 * 			|
		 */

		// this should really be taking into account the fact that this is
		// two doubles being compared, but for now it doesn't
		/*if (lion.getX() < (double) board.getWidth() / 2) {
			xSide = 2;
		} else if (lion.getX() == (double) board.getWidth() / 2) {
			xSide = 1;
		} else {
			xSide = 0;
		}

		if (lion.getY() < (double) board.getHeight() / 2) {
			ySide = 0;
		} else if (lion.getY() == (double) board.getHeight() / 2) {
			ySide = 1;
		} else {
			ySide = 2;
		}

		double xMove = Random.uniform.nextDoubleFromTo(0, 20);
		double yMove = Random.uniform.nextDoubleFromTo(0, 20);

		int mod = (direction == CLOSER) ? 1 : -1;

		double newX = lion.getX(), newY = lion.getY();

		if (xSide == 0) {
			if (ySide == 0) {
				// first quadrant
				newX = (lion.getX() - xMove * mod);
				newY = (lion.getY() + yMove * mod);
			} else {
				// fourth quadrant
				newX = (lion.getX() - xMove * mod);
				newY = (lion.getY() - yMove * mod);
			}
		} else if (xSide == 2) {
			if (ySide == 0) {
				// second quadrant
				newX = (lion.getX() + xMove * mod);
				newY = (lion.getY() + yMove * mod);
			} else {
				// third quadrant
				newX = (lion.getX() + xMove * mod);
				newY = (lion.getY() - yMove * mod);
			}
		} else {
			// on the y axis
			if (ySide == 0)
				newY = (lion.getY() + yMove * mod);
			else if (ySide == 2)
				newY = (lion.getY() - yMove * mod);
			else {
				if (direction == CLOSER) {
					// do nothing, it is already dead center
				} else {
					newX = (lion.getX() - xMove * mod);
					newY = (lion.getY() - yMove * mod);
				}
			}
		}

		if (newX < -(lion.getWidth() / 2.0))
			newX = -(lion.getWidth() / 2.0);
		else if (newX > board.getWidth() + lion.getWidth() / 2.0)
			newX = board.getWidth() + lion.getWidth() / 2.0;

		if (newY < -(lion.getHeight() / 2.0))
			newY = -(lion.getHeight() / 2.0);
		else if (newY > board.getHeight() - lion.getHeight() / 2.0)
			newY = board.getHeight() - lion.getHeight() / 2.0;

		lion.setX(newX);
		lion.setY(newY);*/
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
		getEnergy();

		// figure out what action the network should've performed, so we can
		// train it to perform that correct action. Note, while the network
		// is supposed to be learning XOR, we don't have to put that logic into
		// this agent. The agent just knows whether or not it was scolded in
		// the last step. If the agent wasn't scolded then it will train the
		// network to produce the same behavior again; if it was scolded it will
		// train the network to perform a different action.

		//PRAG 03/17/2012
		//actionShouldveBeen = doHunt();//this.actionPerformedInStep;
		actionShouldveBeen = this.actionPerformedInStep;

		this.net.getNet().getMonitor().setLearningRate(.5);
		this.net.getNet().getMonitor().setMomentum(0.3);

		// compute the network's error
		this.error = Math.abs(actionShouldveBeen - retrievedValue);
		//		System.out.println("Error: " + error+"   Retrieved" + retrievedValue +"    Actionshldhavbeen"+ actionShouldveBeen);
		// System.out.println();
		// get the object that watches over the training
		Monitor monitor = net.getNet().getMonitor();

		// set the monitor parameters
		monitor.setTrainingPatterns(1);
		monitor.setTotCicles(1);

		// setup the inputs for the next round of training
		this.desiredNetworkOutput
		.setInputArray(new double[][] { { actionShouldveBeen } });

		this.inputForTraining
		.setInputArray(properInfut());


		// now actually train the network
		try {
			this.net.train(inputForTraining);
		} catch (NeuralException ex) {
			SimUtilities.showError("Error training neural network for agent \""
					+ getNodeLabel() + "\"", ex);
			throw ex;
		}

		//System.out.println("Training done");
	}

	/**
	 * @return returns the neural network's error
	 */
	public double getError() {
		return error;
	}

	public double getActionPerformed() {
		return actionPerformedInStep;
	}

	public int getEnergy(){
		java.util.Random r = new java.util.Random();
		int Low = 0;
		int High = 100;
		int R = r.nextInt(High-Low) + Low;
		energy = R;
		return R;

	}

	private int doHunt(){
		Lion mom = board.getMother();
		ArrayList<Animat> animats = mom.scanArea(Constant.HUNTRANGE);
		//consider distance also
		if(!(animats.isEmpty()))
		{ 
			for(Animat animat: animats)
			{
				if(animat.animatType != mom.animatType)
				{
					int size = animat.getSize();
					double dist = board.getDistance(mom, animat);
					double ratio = dist/Constant.HUNTRANGE;
					if(energy>50 && size==Constant.smallSize)
						return DO_HUNT;
					else if(energy>70 && size==Constant.mediumSize && ratio>0.5)
						return DO_HUNT;
					else if(energy>90 && size==Constant.largeSize && ratio>0.75)
						return DO_HUNT;
				}
			}
		}
		return DONT_HUNT;
	}

	public boolean killLion()
	{
		if(!(board.lions.isEmpty()))
		{
			board.lions.remove(this);
			NeuralModel.model.removeObject(this);	
			return true;
		}
		return false;
	}

	//3.Scan for animals near Lion
	public ArrayList<Animat> scanArea(int RANGE)
	{
		this.energy -=10;
		if(!alRange.isEmpty())
			alRange.clear();
		for (Iterator iter = board.getGoats().iterator(); iter.hasNext();) 
		{
			Animat animat = (Animat) iter.next();
			if(animat.getX()>=this.getX()-RANGE && animat.getX()<=this.getX()+ RANGE)
			{
				if(animat.getY()>=this.getY()-RANGE && animat.getY()<=this.getY()+ RANGE)
				{
					//					if(goat.getX()!=this.getX() || goat.getY()!=this.getY())
					//					{
					alRange.add(animat);
					//					}
				}
			}
		}

		for (Iterator iter = board.getBuffalos().iterator(); iter.hasNext();) 
		{
			Animat animat = (Animat) iter.next();
			if(animat.getX()>=this.getX()-RANGE && animat.getX()<=this.getX()+ RANGE)
			{
				//System.out.println("SCAN AREA 1");
				if(animat.getY()>=this.getY()-RANGE && animat.getY()<=this.getY()+ RANGE)
				{
					alRange.add(animat);
				}
			}
		}
		return alRange; 
	}

	protected double normalizedCordinates(){
		Lion mom = board.getMother();
		ArrayList<Animat> animats = mom.scanArea(Constant.HUNTRANGE);
		//consider distance also
		double sum = 0;
		int x = 0;
		int y=0;
		for(Animat animat: animats){
			x+= animat.getPosX();
			y+= animat.getPosY();					
		}
		if(!(animats.isEmpty()))
			sum = (x+y)/animats.size();
		return sum;
	}

	protected double normalizedSize(){

		double size=0; 
		Lion mom = board.getMother();
		ArrayList<Animat> animats = mom.scanArea(Constant.HUNTRANGE);
		//consider distance also
		int val = 0;
		if(animats.size()!=0)
		{
			for(Animat animat: animats){
				val += animat.getSize();				
			}

			size= val/animats.size();
		}
		return size;
	}

	protected double getPos(){
		Lion mom = board.getMother();
		//consider distance also
		double sum = mom.getPosX() + mom.getPosY();
		return sum;

	}

	protected double[] getDouble(Object object){
		double nSize[] = new double[1];
		nSize[0] = Double.valueOf(object.toString()).doubleValue();
		//System.out.println("getDouble"+nSize[0]);
		return nSize;
	}

	protected double[] getInputs(){
		double nSize[] = new double[4];
		nSize[0] = normalizedCordinates();
		nSize[1] = normalizedSize();
		nSize[2] = getPos();
		nSize[3] = energy;
		//System.out.println("done get imputs"+nSize);
		return nSize;

	}

	/*protected double[][] properInfut(){
		double nSize[][] = new double[4][1];
		nSize[0] = getDouble(normalizedCordinates());
		nSize[1] = getDouble(normalizedSize());
		nSize[2] = getDouble(getPos());
		nSize[3] = getDouble(getEnergy());
		return nSize;
	}*/

	protected double[][] properInfut(){
		double nSize[][] = new double[1][4];
		nSize[0][0] = normalizedCordinates();
		nSize[0][1] = normalizedSize();
		nSize[0][2] = getPos();
		nSize[0][3] = energy;
		return nSize;
	}

}
