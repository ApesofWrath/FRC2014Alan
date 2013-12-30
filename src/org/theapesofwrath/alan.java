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

/* Port Assignments
 PWMs
 PWM 1 - backLeftTalonPort
 PWM 2 - frontLeftTalonPort
 PWM 3 - shooterMotor0PWM
 PWM 4 - shooterMotor1PWM
 PWM 5 - frontRightTalonPort
 PWM 8 - backRightTalonPort
 Digital IOs
 IO 1 - compressorRelay
 IO 4 - pressureSwitch
 IO 11 - shooterEncoderA
 IO 12 - shooterEncoderB
 Solenoids
 Solenoid 1 - shooterPlungerOut
 Solenoid 2 - shooterPlungerIn
 Solenoid 3 - shooterLiftDown 
 Solenoid 4 - shooterLiftUp 
 Solenoid 5 - shiftUp 
 Solenoid 6 - shiftDown 
 */

 package org.theapesofwrath;

import edu.wpi.first.wpilibj.AnalogChannel;
 import edu.wpi.first.wpilibj.SimpleRobot;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.Talon;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.Solenoid;
 import edu.wpi.first.wpilibj.SpeedController;
 import edu.wpi.first.wpilibj.camera.AxisCamera;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SimpleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class alan extends SimpleRobot {
    
//declaring PWMs
 static final int frontRightTalonPort = 5;
 static final int frontLeftTalonPort = 2;
 static final int backRightTalonPort = 8;
 static final int backLeftTalonPort = 1;
 static final int shooterMotor0PWM = 3;
 static final int shooterMotor1PWM = 4;
 
//declaring digital IOs
 static final int pressureSwitch = 4;
 static final int compressorRelay = 1;
 static final int shooterEncoderA = 11;
 static final int shooterEncoderB = 12;
 
//declaring buttons
 static final int shiftUpButton = 3;
 static final int shiftDownButton = 2;
 static final int shooterUpButton = 6;
 static final int shooterDownButton = 7;
 static final int operatorStartStateMachineButton = 8;
 static final int operatorStopStateMachineButton = 9;
 static final int operatorDumpAStateMachineButton = 10;
 static final int operatorDumpBStateMachineButton = 11;

//various Constants
 final int MAX_RPM = 5000;
 final int DUMP_RPM = 3000;
 final double SHOOTER_TOLERANCE = 50; //RPM
 final double SHOOTER_CYLINDER_RETRACT_TIME = 0.2; // seconds, a guess
 final double SHOOTER_CYLINDER_EXTEND_TIME = 0.2;// seconds, a guess
 final double SHOOTER_MOTOR_SPINUP_WAIT = 4.0; // seconds, a guess
 final double MAX_SHOOTER_MOTOR = -1.0;
 final double MOTOR_STOP = 0.0;
 final double RPM_CONV_PULSE = 60.0 / 24.0 * 1.0 / 128.0 * 60.0; // Gear Ratio * 1/(ticks per Rev.) * 60 sec/min
 
//joysticks
 Joystick leftStick = new Joystick(1);
 Joystick rightStick = new Joystick(2);
 Joystick operatorStick = new Joystick(3);
 
//talons
//drive talons
 Talon frontRightTalon = new Talon(frontRightTalonPort);
 Talon frontLeftTalon = new Talon(frontLeftTalonPort);
 Talon backRightTalon = new Talon(backRightTalonPort);
 Talon backLeftTalon = new Talon(backLeftTalonPort);
//shooter talons
 Talon shooterMotor0 = new Talon(shooterMotor0PWM);
 Talon shooterMotor1 = new Talon(shooterMotor1PWM);
// makes a virtual motor controller hold the values from PID
//class and new object from interface
 SpeedController shooterMotorVirtual = new SpeedController() {
  double speed = 0.0;
     
  public double get() {
  	return speed;
    }
 
  public void set(double speed, byte syncGroup) {
  	set(speed);
    }
 
  public void set(double speed) {
    this.speed = speed;
    shooterMotor0.set(speed);
    shooterMotor1.set(speed);

   }
 
  public void disable() {
  	set(0);
   }
 
  public void pidWrite(double output) {
	set(output);
   }
 };//end shooterMotorVirtual
 
 Encoder shooterEncoder = new Encoder(shooterEncoderA, shooterEncoderB);
 
 RobotDrive chassis = new RobotDrive(frontLeftTalon, backLeftTalon, frontRightTalon, backRightTalon);

 Compressor air = new Compressor(pressureSwitch, compressorRelay);
 
//solenoids
 Solenoid shiftUp = new Solenoid(5);
 Solenoid shiftDown = new Solenoid(6);
 Solenoid shooterLiftUp = new Solenoid(4);
 Solenoid shooterLiftDown = new Solenoid(3);
 Solenoid shooterPlungerIn = new Solenoid(2);
 Solenoid shooterPlungerOut = new Solenoid(1);
 
//state machine, should be enum
 final int start = 0, idle = 1, startShooterMotor = 2,
 shooterMotorSpinupWait = 3, extendCylinder = 4,
 extendCylinderWait = 5, retractCylinder = 6,
 retractCylinderWait = 7, checkFrisbeeCount = 8,
 stopShooterMotor = 9, shooterCheckTrigger = 10,
 setAutoShoot = 11, setManualShoot = 12, setDumpShoot = 13;
 final int dump = 0, manualShoot = 1, autoShoot = 2,
 autonomous = 3, noMode = 4;
 
//only for state machine, but outside so they persist over multiple instances of the loop
 int remainingFrisbees = 4;
//shootingState for state machine
 int shootingState = start;
//shootingMode determines route for cases within state machine
 int shootingMode = noMode;

 Timer shooterTimer = new Timer();
 
/* PID
Proportional value: 0.070
Integral value: 0.00
Derivative value: ~0.500
SampleRate: 0.729
SampleRate (s): .729 / 5 * (1.0 - 0.001) + 0.001 = .1467 seconds
*/
 double kP = 0.070, kI = 0.000, kD = 0.500, sampleRate = .1467;
 int rpm, target;
 PIDController shooterPID = new PIDController(kP, kI, kI, shooterEncoder, shooterMotorVirtual, sampleRate);
 
 AxisCamera camera = AxisCamera.getInstance("10.6.68.11");
 
 //only called once when the robot is first turned on
 public void robotInit() {
  air.start();
  shooterPID.setInputRange(0, MAX_RPM);
  shooterPID.setOutputRange(0, -1.0);
  shooterPID.setAbsoluteTolerance(SHOOTER_TOLERANCE);
  shooterEncoder.setDistancePerPulse(RPM_CONV_PULSE);
  shooterEncoder.setPIDSourceParameter(Encoder.PIDSourceParameter.kRate);
 }//end robotInit
 
 public void autonomous() {
//no autonomous?
 }//end autonomous
 
 public void operatorControl() {
  System.out.println("Operator Control Begin");
  chassis.setSafetyEnabled(true);
   while (isOperatorControl() && isEnabled()) {
 //process the Parts of the Robot in different functions
    chassis.tankDrive(leftStick, rightStick);
    pneumatics();
    shooterStateMachine();
 //Timer.delay(0.01);
   }//end while loop
 }//end operatorControl
 
 public void test() {
  DriverStationLCD dslcd = DriverStationLCD.getInstance();
  Timer pause = new Timer();
  pause.stop();
  int[] cnt = new int[]{0, 0, 0, 0, 0};
  Talon pwm = new Talon(1);
  AnalogChannel ac = new AnalogChannel(1);
  DigitalInput di = new DigitalInput(1);
  Solenoid s = new Solenoid(1);
  Relay r = new Relay(1);
   for (int i = 1; i <= Math.max(Math.max(Math.max(DriverStationLCD.kAnalogChannels, DriverStationLCD.kDigitalChannels), Math.max(DriverStationLCD.kSolenoidChannels, DriverStationLCD.kRelayChannels)), DriverStationLCD.kPwmChannels); i++) {
    if (i <= DriverStationLCD.kPwmChannels) {
     pwm = new Talon(i);
     pwm.set(0.3);
     cnt[0] = i;
    }
   if (i <= DriverStationLCD.kAnalogChannels) {
    ac = new AnalogChannel(i);
    cnt[1] = i;
   }
   if (i <= DriverStationLCD.kDigitalChannels) {
    di = new DigitalInput(i);
    cnt[2] = i;
   }
   if (i <= DriverStationLCD.kRelayChannels) {
    r = new Relay(i);
    r.set(Relay.Value.kForward);
    cnt[3] = i;
   }
  if (i <= DriverStationLCD.kRelayChannels) {
   s = new Solenoid(i);
   s.set(true);
   cnt[4] = i;
  }
 dslcd.println(DriverStationLCD.Line.kUser1, 1, "i: " + String.valueOf(i));
 dslcd.println(DriverStationLCD.Line.kUser2, 1, "pwm " + String.valueOf(cnt[0]) + ": " + String.valueOf(pwm.get()));
 dslcd.println(DriverStationLCD.Line.kUser3, 1, "anlg " + String.valueOf(cnt[1]) + ": " + String.valueOf(ac.getValue()));
 dslcd.println(DriverStationLCD.Line.kUser4, 1, "digi " + String.valueOf(cnt[2]) + ": " + String.valueOf(di.get()));
 dslcd.println(DriverStationLCD.Line.kUser5, 1, "rely " + String.valueOf(cnt[3]) + ": " + String.valueOf(s.get()));
 dslcd.println(DriverStationLCD.Line.kUser6, 1, "sol " + String.valueOf(cnt[4]) + ": " + String.valueOf(r.get().value));
 dslcd.updateLCD();
 pause.reset();
 pause.start();
// go for 1 second
  while (pause.get() < 1.0){
   pause.stop();
   pwm.set(0.0);
   s.set(false);
   r.set(Relay.Value.kOff);
  }
//release 
   pwm.free();
   ac.free();
   di.free();
   s.free();
   r.free();
//reclaims memory of the test objects that were created
   System.gc();
  } // end For loop
 }//end test

// handle the shifting and lifting of the shooter
 private void pneumatics() {
//shift
   if (leftStick.getRawButton(shiftUpButton) || rightStick.getRawButton(shiftUpButton)) {
//shift up solenoids
    shiftUp.set(true);
    shiftDown.set(false);
   }//end if loop for shifting up solenoids
   if (leftStick.getRawButton(shiftDownButton) || rightStick.getRawButton(shiftDownButton)) {
//shift down solenoids
    shiftUp.set(false);
    shiftDown.set(true);
   }//end if loop for shifting down solenoids
   
// Shooter Lifter
 if (operatorStick.getRawButton(shooterUpButton)) {
  shooterLiftUp.set(true);
  shooterLiftDown.set(false);
 }//end if loop for lifting up shooter
 if (operatorStick.getRawButton(shooterDownButton)) {
  shooterLiftUp.set(false);
  shooterLiftDown.set(true);
 }//end if loop for lifting(?) down shooter 
}//end pneumatics

// Extend/contract the plunger for the shooter
 private void plunge(boolean out) {
 shooterPlungerIn.set(!out);
 shooterPlungerOut.set(out);
}//end plunge

// method to put the virtual motor speed into use
// State Mahine to handle all of the shooting
// TODO: Test, this is basically a direct port from the C++ source
 private void shooterStateMachine() {
//compare entry and exit states, detect when state has changed 
  int oldState;
//static unsigned int stateChangeCount=0;
  oldState = shootingState;
  if (operatorStick.getRawButton(operatorStartStateMachineButton)) {
   shootingState = start;
 }// end if loop that changes shootingState to start if operatorStartStateMachineButton is enabled
  switch (shootingState) {
  default:
  shootingState = start;
  break;

//start to idle
 case start:
  shootingState = idle;
  shooterPID.setSetpoint(0);
 // retract cylinder, just in case.
  plunge(false); 
 break;
 
//from start OR stopShooterMotor to setManualShoot OR setDumpShoot
 case idle:
//cout<<"Idling"<<endl;
// stop the motor when we aren't shooting
  shooterMotorVirtual.set(MOTOR_STOP);
   if (operatorStick.getRawButton(operatorStartStateMachineButton)) {
    shootingState = setManualShoot;
   }
   if (operatorStick.getRawButton(operatorDumpAStateMachineButton)
    || operatorStick.getRawButton(operatorDumpBStateMachineButton)) {
     shootingState = setDumpShoot;
   }
 break;

//from idle to startShooterMotor
 case setManualShoot:
  shootingMode = manualShoot;
  shootingState = startShooterMotor;
  shooterPID.setSetpoint(MAX_RPM * (-(1 - operatorStick.getThrottle()) / 2));
 break;

//fom idle to startShooterMotor
 case setDumpShoot:
  shootingMode = dump;
  shootingState = startShooterMotor;
 break;

//from startShooterMotor OR retractCylinderWait to extendCylinder
 case shooterCheckTrigger:
//buttton one is trigger
//cout<<"checking trigger"<<endl;
  shooterPID.setSetpoint(MAX_RPM * (-(1 - operatorStick.getThrottle()) / 2));
   if (operatorStick.getRawButton(1)) {
    shootingState = extendCylinder;
 }
/*else {
shootingState = idle;
}*/
 break;
//from setManualShoot OR setDumpShoot to shooterMotorSpinupWait OR shooterCheckTrigger
 case startShooterMotor:
  shooterPID.setSetpoint(DUMP_RPM);
   if (shootingMode == dump) {
     shooterTimer.reset();
     shootingState = shooterMotorSpinupWait;
   }//end if (dump)
/* for manual shooting, the operator has to set the shooter motor speed manually. 
It is expected that the operator will wait for spinup before pulling the trigger*/
   if (shootingMode == manualShoot) {
    shootingState = shooterCheckTrigger;
   }
   
 break;

//from startShooterMotor to extendCylinder
 case shooterMotorSpinupWait:
  if (shooterTimer.get() >= SHOOTER_MOTOR_SPINUP_WAIT) {
    shootingState = extendCylinder;
  }
 break;
 
//from shooterMotorSpinupWait OR shooterCheckTrigger OR checkFrisbeeCount to extendCylinderWait 
 case extendCylinder:
//cout<<"firing"<<endl;
  plunge(true); 
  shooterTimer.reset();
  shootingState = extendCylinderWait;
 break;

//from extendCylinder to retractCylinder 
 case extendCylinderWait:
  if (shooterTimer.get() >= SHOOTER_CYLINDER_EXTEND_TIME) {
   shooterTimer.reset();
   shootingState = retractCylinder;
 }
 break;

//from extendCylinderWait to retractCylinderWait 
 case retractCylinder:
  plunge(false);
  shooterTimer.reset();
  shootingState = retractCylinderWait;
 break;
 
//from retractCylinder to checkFrisbeCount OR shooterCheckTrigger
 case retractCylinderWait:
  if (shooterTimer.get() >= SHOOTER_CYLINDER_RETRACT_TIME) {
    shooterTimer.reset();
  if (shootingMode == dump) {
   shootingState = checkFrisbeeCount;
  }
  if (shootingMode == manualShoot) {
   shootingState = shooterCheckTrigger;
  }
 }
 break;

//from retractCylinderWait to stopShooterMotor OR extendCylinder 
 case checkFrisbeeCount:
  --remainingFrisbees;
//cout<<"remaing frisbees"<<remainingFrisbees<<endl;
//number of frisbees decrement when?
   if (remainingFrisbees <= 0) {
    remainingFrisbees = 4;
    shootingState = stopShooterMotor;
   } 
   else {
    shootingState = extendCylinder;
   }
 break;

//from checkFrisbeeCount to idle                        
 case stopShooterMotor:
//cout<<"stopping";
  shooterMotorVirtual.set(MOTOR_STOP);
  shootingState = idle;
 break;
  }//end Switch(shootingState)
 }// end shooterController
}// end public class alan
