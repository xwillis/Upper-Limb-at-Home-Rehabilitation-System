package com.example.nutri_000.testinggauge;

public class ArmCalculator
{
    //class variables
    /*int[] zyzChest={0,0,0};
    int[] zyzBicep={0,0,0};
    int[] zyzWrist={0,0,0};
    int[] zyzHand={0,0,0};*/

    double[] xyzChest={0,0,0};
    double[] xyzBicep={0,0,0};
    double[] xyzWrist={0,0,0};
    double[] xyzHand={0,0,0};

    double[] deltaChestBicep={0,0,0};
    double[] deltaBicepWrist={0,0,0};
    double[] deltaWristHand={0,0,0};

    static double[] jointAngles={0,0,0,0,0,0,0};


    //constructor
    public ArmCalculator(){
        //new armCalculator Objext created with default values
    }
    public ArmCalculator(double[] chestValues, double[] bicepValues, double[] wristValues, double[] handValues){
        xyzChest=chestValues;
        xyzBicep=bicepValues;
        xyzWrist=wristValues;
        xyzHand=handValues;
    }
    private double[] notificationToXYZArray(BleNotification notification) {
        double[] values=new double[3];
        values[0]=notification.valueX;
        values[1]=notification.valueY;
        values[2]=notification.valueZ;
        return values;
    }
    public double[] updateChest(BleNotification notification){
        xyzChest=notificationToXYZArray(notification);
        return findJointAngles();
    }

    public double[] updateBicep(BleNotification notification){
        xyzChest=notificationToXYZArray(notification);
        return findJointAngles();
    }
    public double[] updateWrist(BleNotification notification){
        xyzChest=notificationToXYZArray(notification);
        return findJointAngles();
    }
    public double[] updateHand(BleNotification notification){
        xyzChest=notificationToXYZArray(notification);
        return findJointAngles();
    }
    public double[] findJointAngles(){
        deltaChestBicep=subtractIMU2fromIMU1(xyzBicep, xyzChest);
        deltaBicepWrist=subtractIMU2fromIMU1(xyzWrist,xyzBicep);
        deltaWristHand=subtractIMU2fromIMU1(xyzHand,xyzWrist);
        jointAngles=thetasForJointAngles(deltaChestBicep, deltaBicepWrist, deltaWristHand);
        return jointAngles;
    }

			//converting zyz radian values to zyz degree values
			public static double[] convertZYZRadtoZYZDegrees(double[] zyz)
			{
				double[] zyzDeg = new double[3];
				
				zyzDeg[0] = Math.toDegrees(zyz[0]);
				zyzDeg[1] = Math.toDegrees(zyz[1]);
				zyzDeg[2] = Math.toDegrees(zyz[2]);
				
				//return in degrees 
				return zyzDeg;
			}
			
			
			//convert zyz degrees to xyz degrees 
			public static double[] convertZYZDegreesToXYZDegrees(double[] zyz)
			{
				double[] IMU_xyz=new double[3];
				IMU_xyz[0] =   Math.asin(Math.cos(zyz[0])*Math.sin(zyz[1]));
				IMU_xyz[1] =   Math.atan(-Math.sin(zyz[0])*Math.sin(zyz[1]) / Math.cos(zyz[1]));
				IMU_xyz[2] =   Math.atan(Math.cos(zyz[0])*Math.cos(zyz[1])*Math.sin(zyz[2]) + Math.sin(zyz[0])*Math.cos(zyz[2]) / Math.cos(zyz[0])*Math.cos(zyz[1])*Math.cos(zyz[2]) - Math.sin(zyz[0])*Math.sin(zyz[2]));
				return IMU_xyz;
			}
			
			
			
			//function for the differences in sensors, paramaters include IMU2 and IMU1
			public static double[] subtractIMU2fromIMU1(double[] IMU2, double[] IMU1 )
			{
				//array of 3 doubles x, y, z
				double[] IMU_delta = new double[3];
				
				//iterates from 0 to 2
				for(int i = 0; i < 3; i++)
				{
					//subtract each pair x, y, z 
					IMU_delta[i] = IMU2[i] - IMU1[i];
				}
				
				//will return the difference 
				return IMU_delta;
				
				
			}
			
			
			//this is for the rotation matrix function, parameter DeltaMatrix 
			/*public static double[][] RotationMatrixUsingDelta(double[] DeltaMatrix)
			
			{
				
			//creating the 3 by 3 matrix
			double[][] Matrix = 
				{
				
				//the input values inside the () would access the Xn , Yn , Zn and their respective delta values from the 2nd - 1st sensor
				//3rd - 2nd sensor 
				//4th - 3rd sensor
				//will return Matrix (i.e array of array)
				{ Math.cos(DeltaMatrix[1])*Math.cos(DeltaMatrix[2]) , -Math.cos(DeltaMatrix[1])*Math.sin(DeltaMatrix[2]) , Math.sin(DeltaMatrix[1]) } , 
				
					
				{Math.sin(DeltaMatrix[0])*Math.sin(DeltaMatrix[1])*Math.cos(DeltaMatrix[2]) , -Math.sin(DeltaMatrix[0])*Math.sin(DeltaMatrix[1])*Math.sin(DeltaMatrix[2]) + Math.cos(DeltaMatrix[0])*Math.cos(DeltaMatrix[2]) , -Math.sin(DeltaMatrix[0])*Math.cos(DeltaMatrix[1]) } 
						,  
					
			    {-Math.cos(DeltaMatrix[0])*Math.sin(DeltaMatrix[1])*Math.cos(DeltaMatrix[2]) + Math.sin(DeltaMatrix[0])*Math.sin(DeltaMatrix[2]) , 
				   
				   Math.cos(DeltaMatrix[0])*Math.sin(DeltaMatrix[1])*Math.sin(DeltaMatrix[2]) + Math.sin(DeltaMatrix[0])*Math.cos(DeltaMatrix[2]) , 
				   
				   Math.cos(DeltaMatrix[0])*Math.cos(DeltaMatrix[1]) } 
					
				};
			
				return Matrix;
				
			}*/

			
			
			public static double[] thetasForJointAngles(double[] deltaCB, double[] deltaBW, double[] deltaWH)

			{

                double r112=-Math.cos(deltaCB[1])*Math.sin(deltaCB[2]);
                double r123=-Math.sin(deltaCB[0])*Math.cos(deltaCB[1]);
                double r122=-Math.sin(deltaCB[0])*Math.sin(deltaCB[1])*Math.sin(deltaCB[2]) + Math.cos(deltaCB[0])*Math.cos(deltaCB[2]);
                double r121=Math.sin(deltaCB[0])*Math.sin(deltaCB[1])*Math.cos(deltaCB[2]);
                double r212=-Math.cos(deltaBW[1])*Math.sin(deltaBW[2]);
                double r222=-Math.sin(deltaBW[0])*Math.sin(deltaBW[1])*Math.sin(deltaBW[2]) + Math.cos(deltaBW[0])*Math.cos(deltaBW[2]);
                double r231=-Math.cos(deltaBW[0])*Math.sin(deltaBW[1])*Math.cos(deltaBW[2]) + Math.sin(deltaBW[0])*Math.sin(deltaBW[2]);
                double r233=Math.cos(deltaBW[0])*Math.cos(deltaBW[1]);
                double r313=Math.sin(deltaWH[1]);
                double r323=-Math.sin(deltaWH[0])*Math.cos(deltaWH[1]);
                double r331=-Math.cos(deltaWH[0])*Math.sin(deltaWH[1])*Math.cos(deltaWH[2]) + Math.sin(deltaWH[0])*Math.sin(deltaWH[2]);
                double r332=Math.cos(deltaWH[0])*Math.sin(deltaWH[1])*Math.sin(deltaWH[2]) + Math.sin(deltaWH[0])*Math.cos(deltaWH[2]);


				//using the 3 by 3 matrix but based off of how many elements will need to subtract 1 from each function of theta (i.e based off matrix Ex: r12 or r32) 
				//in order to produce proper joint value 
				jointAngles[0]=Math.atan2(-r112, r123);
				jointAngles[1]=Math.asin(r122);
				jointAngles[2]=Math.atan2(r121,-r123);
				jointAngles[3]=Math.atan2(-r212,r222);
				jointAngles[4]=Math.atan2(-r231, r233);
				jointAngles[5]=Math.atan2(r313,r323);
				jointAngles[6]=Math.atan2(r331,r332);

				

				
				return jointAngles;
				
			}
			
			
				// converts a given double array to a string representation with a trailing space
				/*public static String doubleArrayToString(double[] array)
				{
					String ret = "";
					for (double x : array)
						ret += x + " ";
					return ret;
				}
				
				
				//matrix into a string 
				public static String doubleMatrixToString(double[][] array) 
				{
					//access the array which would access the next array of the 3 by 3 matrix
					String ret = "";
					for(double[] y : array) 
					
					{
					for (double x : y)
					{
						ret += x + " ";
					
					} ret +=  '\n';} 
					return ret;
				}*/
}
