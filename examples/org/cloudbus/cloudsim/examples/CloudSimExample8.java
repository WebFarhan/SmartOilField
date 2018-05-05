/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


package org.cloudbus.cloudsim.examples;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * An example showing how to create simulation entities
 * (a DatacenterBroker in this example) in run-time using
 * a globar manager entity (GlobalBroker).
 */
public class CloudSimExample8 {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList1;
	
	private static List<Cloudlet> cloudletList2; //added by OOA
	private static List<Vm> vmList2,vmList1; //added by OOA


	/** The vmList. */
	private static List<Vm> vmList;

	private static List<Vm> createVM(int userId, int vms, int idShift) {
		//Creates a container to store VMs. This list is passed to the broker later
		LinkedList<Vm> list = new LinkedList<Vm>();

		//VM Parameters
		long size = 10000; //image size (MB)
		int ram = 5000; //vm memory (MB)
		int mips = 250;
		long bw = 1000;
		int pesNumber = 1; //number of cpus
		String vmm = "Xen"; //VMM name

		//create VMs
		Vm[] vm = new Vm[vms];

		for(int i=0;i<vms;i++){
			vm[i] = new Vm(idShift + i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			list.add(vm[i]);
		}

		return list;
	}

	private static int showRandomInteger(int aStart, int aEnd, Random aRandom){
	    if (aStart > aEnd) {
	      throw new IllegalArgumentException("Start cannot exceed End.");
	    }
	    //get the range, casting to long to avoid overflow problems
	    long range = (long)aEnd - (long)aStart + 1;
	    // compute a fraction of the range, 0 <= frac < range
	    long fraction = (long)(range * aRandom.nextDouble());
	    int randomNumber =  (int)(fraction + aStart);    
	    
	    return randomNumber;
	  }

	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}

	private static double showRandomDouble(double aStart, double aEnd){
	    if (aStart > aEnd) {
	      throw new IllegalArgumentException("Start cannot exceed End.");
	    }

	    Random r = new Random();
	    double randomValue = aStart + (aEnd - aStart) * r.nextDouble();
	    
	    
	    return round(randomValue, 2);//randomValue;
	  }
	
	
	private static List<Cloudlet> createCloudlet(int userId, int cloudlets, int START, int END, int idShift, long seed){
		// Creates a container to store Cloudlets
				LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

				//tasks(Cloudlets) parameters
				/*Task (Cloudlets) parameters*/
				
				long length; 												/* MI of the Cloudlet */
				long fileSize = 540000;
				long outputSize = 300;
				int pesNumber = 1;
				double deadline = 0.0;
				double priority = 0.0;
				double xVal=0.0;
				int taskType = 0;
				//long seed1 = 500;

				//long timeMillis = System.currentTimeMillis();
		        //long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
				
				UtilizationModel utilizationModel = new UtilizationModelFull();

				Cloudlet[] cloudlet = new Cloudlet[cloudlets];

				for(int i=0;i<cloudlets;i++){
					//long timeMillis = System.currentTimeMillis(); //replace with relative time to simulator
			        //long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
					Random rObj = new Random();
					rObj.setSeed(seed);
					deadline = showRandomDouble(0.4, 1.5);
					priority = Math.pow((1/Math.E),deadline);
					length = 1000+showRandomInteger(START, END,rObj);
					
					if(length <= 1500)
					{
						taskType = 1;
					}
					else if(length > 1500)
					{
						taskType = 2;
					}
					
					xVal = showRandomInteger(1,4,rObj);
					cloudlet[i] = new Cloudlet(taskType,idShift+i,length,deadline,priority,xVal,showRandomInteger(0,1,rObj),
							showRandomInteger(120,120,rObj),pesNumber, fileSize +showRandomInteger(15000, 25000,rObj), outputSize, utilizationModel, 
							utilizationModel, utilizationModel,0);
					cloudlet[i].setUserId(userId);		/* Setting the owner of these Cloudlets */
					list.add(cloudlet[i]);
					seed--;
				}

				return list;
	}


	////////////////////////// STATIC METHODS ///////////////////////

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		Log.printLine("Starting CloudSimExample8...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 2;   // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			GlobalBroker1 globalBroker1 = new GlobalBroker1("GlobalBroker_1");
			
			DatacenterBroker broker2 = createBroker("Broker_2");
			int brokerId2 = broker2.getId();
			
			GlobalBroker2 globalBroker2 = new GlobalBroker2("GlobalBroker_2");

			// Second step: Create Datacenters
			//Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
			@SuppressWarnings("unused")
			Datacenter datacenter0 = createDatacenter("Datacenter_0");
			@SuppressWarnings("unused")
			Datacenter datacenter1 = createDatacenter("Datacenter_1");

			//Third step: Create Broker
			DatacenterBroker broker1 = createBroker("Broker_1");
			int brokerId1 = broker1.getId();

			//DatacenterBroker broker2 = createBroker("Broker_2");
			//int brokerId2 = broker2.getId();
			
			
			//Fourth step: Create VMs and Cloudlets and send them to broker
			vmList1 = createVM(brokerId1, 5, 0); //creating 5 vms
			//vmList2 = createVM(brokerId2, 5, 0); 
			
			cloudletList1 = createCloudlet(brokerId1, 5,100,1000,1,400); // creating 10 cloudlets
			//cloudletList2 = createCloudlet(brokerId1, 5,100,1000,800,500); // creating 10 cloudlets
			
			
			broker1.submitVmList(vmList1);
			broker1.submitCloudletList(cloudletList1);

			// Fifth step: Starts the simulation
			CloudSim.startSimulation();

			
			
			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker1.getCloudletReceivedList();
			newList.addAll(globalBroker1.getBroker().getCloudletReceivedList());
			newList.addAll(globalBroker2.getBroker().getCloudletReceivedList());

			CloudSim.stopSimulation();

			printCloudletList(newList);

			Log.printLine("CloudSimExample8 finished!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}

	private static Datacenter createDatacenter(String name){

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store one or more
		//    Machines
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
		//    create a list to store these PEs before creating
		//    a Machine.
		List<Pe> peList1 = new ArrayList<Pe>();

		int mips = 1000;

		// 3. Create PEs and add these into the list.
		//for a quad-core machine, a list of 4 PEs is required:
		peList1.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
		peList1.add(new Pe(1, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(2, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(3, new PeProvisionerSimple(mips)));

		//Another list, for a dual-core machine
		List<Pe> peList2 = new ArrayList<Pe>();

		peList2.add(new Pe(0, new PeProvisionerSimple(mips)));
		peList2.add(new Pe(1, new PeProvisionerSimple(mips)));

		//4. Create Hosts with its id and list of PEs and add them to the list of machines
		int hostId=0;
		int ram = 16384; //host memory (MB)
		long storage = 1000000; //host storage
		int bw = 10000;

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList1,
    				new VmSchedulerTimeShared(peList1)
    			)
    		); // This is our first machine

		hostId++;

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList2,
    				new VmSchedulerTimeShared(peList2)
    			)
    		); // Second machine

		// 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.1;	// the cost of using storage in this resource
		double costPerBw = 0.1;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, 0 , 0, 0,characteristics, new VmAllocationPolicySimple(hostList), storageList, 100);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	//We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	//to the specific rules of the simulated scenario
	private static DatacenterBroker createBroker(String name){

		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker(name);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects
	 * @param list  list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
				"Data center ID" + indent + "VM ID" + indent + indent +"length"+ indent + indent +"Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
				Log.print("SUCCESS");

				Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
						indent + indent + indent +cloudlet.getCloudletLength()+indent + indent + indent+dft.format(cloudlet.getActualCPUTime()) +
						indent + indent + dft.format(cloudlet.getExecStartTime())+ indent + indent + indent + dft.format(cloudlet.getFinishTime()));
			}
		}

	}

	public static class GlobalBroker1 extends SimEntity {

		private static final int CREATE_BROKER = 0;
		private List<Vm> vmList;
		private List<Cloudlet> cloudletList;
		private DatacenterBroker broker;

		public GlobalBroker1(String name) {
			super(name);
		}

		@Override
		public void processEvent(SimEvent ev) {
			switch (ev.getTag()) {
			case CREATE_BROKER:
				setBroker(createBroker(super.getName()+"_"));

				//Create VMs and Cloudlets and send them to broker
				setVmList(createVM(getBroker().getId(), 4, 100)); //creating 5 vms
				setCloudletList(createCloudlet(getBroker().getId(), 5,100,1000,1,400)); // creating 5 cloudlets

				broker.submitVmList(getVmList());
				broker.submitCloudletList(getCloudletList());

				CloudSim.resumeSimulation();

				break;

			default:
				Log.printLine(getName() + ": unknown event type");
				break;
			}
		}

		@Override
		public void startEntity() {
			Log.printLine(super.getName()+" is starting...");
			//Log.printLine(" Clock time : " + CloudSim.getSimulationCalendar());
			schedule(getId(), 100, CREATE_BROKER);// second parameter define the start time.
		}

		@Override
		public void shutdownEntity() {
		}

		public List<Vm> getVmList() {
			return vmList;
		}

		protected void setVmList(List<Vm> vmList) {
			this.vmList = vmList;
		}

		public List<Cloudlet> getCloudletList() {
			return cloudletList;
		}

		protected void setCloudletList(List<Cloudlet> cloudletList) {
			this.cloudletList = cloudletList;
		}

		public DatacenterBroker getBroker() {
			return broker;
		}

		protected void setBroker(DatacenterBroker broker) {
			this.broker = broker;
		}

	}
	
	
	public static class GlobalBroker2 extends SimEntity {

		private static final int CREATE_BROKER = 0;
		private List<Vm> vmList;
		private List<Cloudlet> cloudletList;
		private DatacenterBroker broker;

		public GlobalBroker2(String name) {
			super(name);
		}

		@Override
		public void processEvent(SimEvent ev) {
			switch (ev.getTag()) {
			case CREATE_BROKER:
				setBroker(createBroker(super.getName()+"_"));

				//Create VMs and Cloudlets and send them to broker
				setVmList(createVM(getBroker().getId(), 4, 100)); //creating 5 vms
				setCloudletList(createCloudlet(getBroker().getId(), 5,100,1000,3000,400)); // creating 5 cloudlets

				broker.submitVmList(getVmList());
				broker.submitCloudletList(getCloudletList());

				CloudSim.resumeSimulation();

				break;

			default:
				Log.printLine(getName() + ": unknown event type");
				break;
			}
		}

		@Override
		public void startEntity() {
			Log.printLine(super.getName()+" is starting...");
			//Log.printLine(" Clock time : " + CloudSim.getSimulationCalendar());
			schedule(getId(), 200, CREATE_BROKER);// second parameter define the start time.
			
		}

		@Override
		public void shutdownEntity() {
		}

		public List<Vm> getVmList() {
			return vmList;
		}

		protected void setVmList(List<Vm> vmList) {
			this.vmList = vmList;
		}

		public List<Cloudlet> getCloudletList() {
			return cloudletList;
		}

		protected void setCloudletList(List<Cloudlet> cloudletList) {
			this.cloudletList = cloudletList;
		}

		public DatacenterBroker getBroker() {
			return broker;
		}

		protected void setBroker(DatacenterBroker broker) {
			this.broker = broker;
		}

	}
	
	

}