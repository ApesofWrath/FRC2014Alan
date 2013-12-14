/**
2014 Team 668 reprogram of Alan in Java

Driver stick 1 interchangeable with driver stick 2
2 : Shift Down
3 : Shift Up
4: Auto Aim same as 5
5 : Auto Aim same as 4
8 : Arcade
9 : Tank

Driver stick 1 interchangeable with driver stick 2
2 : Shift Down
3 : Shift Up
4 : Auto Aim same as 5
5 : Auto Aim same as 4
8 : Arcade
9 : Tank

Operator Stick :
1 : Fire
2 : Top Goal
4 : Left Middle Goal
5 : Right Middle Goal
6 : Shooter Up
7 : Shooter Down
10 : Dump same as 11
11 :Dump same as 10
z : Blast/manual mode

Steering Wheel
Any Button : enable the wheel
*/

package org.theapesofwrath;
import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Compressor;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SimpleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class alan extends SimpleRobot {
    final int frontRightTalonPort = 5;
    final int frontLeftTalonPort = 2;
    final int backRightTalonPort = 8;
    final int backLeftTalonPort = 1;
    
  
    final int pressureSwitch = 4;
    final int compressorRelay = 1; 
    
   final int shiftUpButton = 3;
   final int shiftDownButton = 2;
    
    Joystick leftStick = new Joystick(1);
    Joystick rightStick = new Joystick(2);
    
    Talon frontRightTalon = new Talon(frontRightTalonPort);
    Talon frontLeftTalon = new Talon(frontLeftTalonPort);
    Talon backRightTalon = new Talon(backRightTalonPort);
    Talon backLeftTalon = new Talon(backLeftTalonPort);
    
    RobotDrive chassis = new RobotDrive(frontLeftTalon, backLeftTalon, frontRightTalon, backRightTalon);
    
    Compressor air = new Compressor(pressureSwitch, compressorRelay);
    public void robotInit() {
       air.start();
    }
    
    public void autonomous() {
        
    }

    /**
     * This function is called once each time the robot enters operator control.
     */
    public void operatorControl() {
        System.out.println("Operator Control Begin");
        
        chassis.setSafetyEnabled(true);
        
        while(isOperatorControl() && isEnabled()) {
            chassis.tankDrive(leftStick, rightStick);
            Timer.delay(0.01);
            if(leftStick.getRawButton(shiftUpButton) || rightStick.getRawButton(shiftUpButton)) {
                //shift up solenoids
            }
            if(leftStick.getRawButton(shiftDownButton) || rightStick.getRawButton(shiftDownButton)) {
                //shift down solenoids
            }
            
        }
    }
    
    /**
     * This function is called once each time the robot enters test mode.
     */
    public void test() {
    
    }
    
}
