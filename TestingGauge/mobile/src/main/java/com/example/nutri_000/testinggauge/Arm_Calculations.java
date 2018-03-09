
public class Arm_Calculations 
{

	public static void main(String[] args) 
		
	{
		// TODO Auto-generated method stub
				//arbitrary values for the inputs later used to find delta_12x , delta_12y , delta_12z , delta2_12x , delta2_12y , delta2_12z
				double[] imu1zyzRad = {.5, 1, .75};
				// call a function to convert to degrees
				double[] imu1zyzDeg = convertZYZRadtoZYZDegrees(imu1zyzRad); 		// call a function that takes imu1zyzRad and returns it in degrees
				double[] imu1xyzDeg = convertZYZDegreesToXYZDegrees(imu1zyzDeg);	// call a function that takes imu1zyzDeg and returns it in xyz degrees
				
				
				double[] imu2zyzRad = {.1, 5, .6};
				double[] imu2zyzDeg = convertZYZRadtoZYZDegrees(imu2zyzRad); 		// call a function that takes imu1zyzRad and returns it in degrees
				double[] imu2xyzDeg = convertZYZDegreesToXYZDegrees(imu2zyzDeg);	// call a function that takes imu1zyzRad and returns it in degrees
				

				double[] imu3zyzRad = {.2, .25, .3};
				double[] imu3zyzDeg = convertZYZRadtoZYZDegrees(imu3zyzRad); 		// call a function that takes imu1zyzRad and returns it in degrees
				double[] imu3xyzDeg = convertZYZDegreesToXYZDegrees(imu3zyzDeg);	// call a function that takes imu1zyzRad and returns it in degrees
				
				double[] imu4zyzRad = {.1, .4, .6};
				double[] imu4zyzDeg = convertZYZRadtoZYZDegrees(imu4zyzRad); 		// call a function that takes imu1zyzRad and returns it in degrees
				double[] imu4xyzDeg = convertZYZDegreesToXYZDegrees(imu4zyzDeg);	// call a function that takes imu1zyzRad and returns it in degrees
				
				
				System.out.println(" conversion for 1st sensor");
				//printing the xyz degrees for imu 1 
				System.out.println(doubleArrayToString(imu1xyzDeg));
				System.out.println();
				
				System.out.println(" conversion for 2nd sensor");
				//printing the xyz degrees for imu 2 
				System.out.println(doubleArrayToString(imu2xyzDeg));
				System.out.println();

				System.out.println(" conversion for 3rd sensor");
				//printing the xyz degrees for imu 3
				System.out.println(doubleArrayToString(imu3xyzDeg));
				System.out.println();
				
				System.out.println(" conversion for 4th sensor");
				//printing the xyz degrees for imu 4
				System.out.println(doubleArrayToString(imu4xyzDeg));
				System.out.println();
				
				//printing the difference for 2nd and 1st sensor with respect to xyz coordinates 
				System.out.println(" The difference in 2nd & 1st sensor values with respect to xyz ");
				//
				System.out.println(doubleArrayToString(subtractIMU2fromIMU1(imu2xyzDeg, imu1xyzDeg)));
				System.out.println();

				//printing the difference for 3rd and 2nd sensor with respect to xyz coordinates 
				System.out.println(" The difference in 3rd & 2nd sensor values with respect to xyz ");
				//
				System.out.println(doubleArrayToString(subtractIMU2fromIMU1(imu3xyzDeg, imu2xyzDeg)));
				System.out.println();
				
				//printing the difference for 4th and 3rd sensor with respect to xyz coordinates 
				System.out.println(" The difference in 4th and 3rd sensor values with respect to xyz ");
				System.out.println(doubleArrayToString(subtractIMU2fromIMU1(imu4xyzDeg, imu3xyzDeg)));
				System.out.println();
				
				
				//printing the matrix for 1st and 2nd sensors
				System.out.println(" The following would print the rotation matrix for the 2nd and 1st sensor via the delta values of xyz  ");
				System.out.println(doubleMatrixToString(RotationMatrixUsingDelta(subtractIMU2fromIMU1(imu2xyzDeg, imu1xyzDeg ))));
				System.out.println();
				
				//printing the matrix for 3rd and 2nd sensors
				System.out.println(" The following would print the rotation matrix for the 3rd and 2nd sensor via the delta values of xyz  ");
				System.out.println(doubleMatrixToString(RotationMatrixUsingDelta(subtractIMU2fromIMU1(imu3xyzDeg, imu2xyzDeg ))));
				System.out.println();
				
				//printing the matrix for 4th and 3rd sensors
				System.out.println(" The following would print the rotation matrix for the 4th and 3rd sensor via the delta values of xyz  ");
				System.out.println(doubleMatrixToString(RotationMatrixUsingDelta(subtractIMU2fromIMU1(imu4xyzDeg, imu3xyzDeg ))));
				System.out.println();
				
				//theta values for all joint angles using 1st and 2nd sensor 
				System.out.println("Theta values for the joints "); 
				System.out.println(doubleArrayToString(ThetasForJointAngles(RotationMatrixUsingDelta(subtractIMU2fromIMU1(imu2xyzDeg, imu1xyzDeg))))); 
				System.out.println();

				
				//theta values for all joint angles using 2nd and 3rd sensor 
				System.out.println("Theta values for the joints "); 
				System.out.println(doubleArrayToString(ThetasForJointAngles(RotationMatrixUsingDelta(subtractIMU2fromIMU1(imu3xyzDeg, imu2xyzDeg))))); 
				System.out.println();
				
				
				//theta values for all joint angles using 4th and 3rd sensor 
				System.out.println("Theta values for the joints "); 
				System.out.println(doubleArrayToString(ThetasForJointAngles(RotationMatrixUsingDelta(subtractIMU2fromIMU1(imu4xyzDeg, imu3xyzDeg))))); 
				System.out.println();
				
				
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
				
				// helper method
				return convertZYZDegreesToXYZDegrees(zyz[0], zyz[1], zyz[2]);
				
			}
			
			
			//using the 3 parameters imu1z1,imu1y1,imu1z2, and rest of inputs for other sensors to convert zyz degrees to xyz degrees and return array type 
			public static double[] convertZYZDegreesToXYZDegrees(double imu1z1, double imu1y1, double imu1z2)
			{
				//calculation to convert to the '' xyz '' orientation 
				//for the first IMU
				double[] IMU_xyz=new double[3];
				IMU_xyz[0] =   Math.asin(Math.cos(imu1z1)*Math.sin(imu1y1));
				IMU_xyz[1] =   Math.atan(-Math.sin(imu1z1)*Math.sin(imu1y1) / Math.cos(imu1y1)); 
				IMU_xyz[2] =   Math.atan(Math.cos(imu1z1)*Math.cos(imu1y1)*Math.sin(imu1z2) + Math.sin(imu1z1)*Math.cos(imu1z2) / Math.cos(imu1z1)*Math.cos(imu1y1)*Math.cos(imu1z2) - Math.sin(imu1z1)*Math.sin(imu1z2));
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
			public static double[][] RotationMatrixUsingDelta(double[] DeltaMatrix)
			
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
				
			}

			
			
			public static double[] ThetasForJointAngles(double[][] DeltaMatrix)
			
			
			{
				//creating the index 
				double [] ThetaValues = new double[7];
				
				//using the 3 by 3 matrix but based off of how many elements will need to subtract 1 from each function of theta (i.e based off matrix Ex: r12 or r32) 
				//in order to produce proper joint value 
				ThetaValues[0] = Math.atan(-DeltaMatrix[0][1] / DeltaMatrix[2][1]);
				ThetaValues[1] = Math.asin(DeltaMatrix[1][1]); 
				ThetaValues[2] = Math.atan(DeltaMatrix[1][0] / -DeltaMatrix[1][2]); 
				ThetaValues[3] = Math.atan(-DeltaMatrix[0][1] / DeltaMatrix[1][1]);
				ThetaValues[4] = Math.atan(-DeltaMatrix[2][0] / DeltaMatrix[2][2]);
				ThetaValues[5] = Math.atan(DeltaMatrix[0][2] / DeltaMatrix[1][2]); 
				ThetaValues[6] = Math.atan(DeltaMatrix[2][0] / DeltaMatrix[2][1]);
				

				
				return ThetaValues; 
				
			}
			
			
				// converts a given double array to a string representation with a trailing space
				public static String doubleArrayToString(double[] array) 
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
				}
				
			}
