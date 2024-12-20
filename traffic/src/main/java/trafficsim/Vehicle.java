package trafficsim;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Vehicle implements ActionListener {

    private TrafficLight trafficLight;
    private final ImageObserver imgObserve;
    private Image mImage;                                                //Image of vehicle
    private float velocity, acceleration = 0f, vehVelo;                                                    //Max speed of vehicle
    private float mCurAngle;                                            //Current angle of vehicle relative to normal

    public enum VehicleDirection {
        LEFT, RIGHT, UP, DOWN, DOWN_LEFT, RIGHT_UP,
        RIGHT_DOWN, LEFT_UP, UP_LEFT, UP_RIGHT
    }
    public enum VehicleState {BRAKE, MOVE_X, MOVE_Y, TURNING}
    public enum Command {GO_STRAIGHT, TURN_LEFT, TURN_RIGHT}

    private Vector2 vehiclePosition;                                    //the position of vehicle
    private VehicleState vState;
    private final VehicleState vPrev;                                        //Stores current vehicle state
    private VehicleDirection vDirection;                                //Stores current vehicle direction
    private AffineTransform trans;                                        //The transformation of vehicle

    private boolean passedTrafficLight;

    //Active Cruise Control variables
    private Vehicle vehicleAhead;

    //For controlling turning
    private Command command;

    //Traffic lights position constants that vehicles should pass
    private final int movingRightPos = 301;        //vehicle position.x should be > 351 to continue moving
    private final int movingLeftPos = 1150;        //vehicle position.x should be < 1150 to continue moving
    private final int movingDownPos = 121;        //vehicle position.y should be > 171 to continue moving
    private final int movingUpPos = 652;        //vehicle position.y should be < 582 to continue moving

    //A turning constant
    private boolean turn = false;

    /**
     * Constructor for class
     * Sets the current vehicle state to BRAKE
     * Sets current angle to 0
     *
     * @param src Loads the vehicle image
     * @param vel Sets the vehicle max speed
     */

    public Vehicle(InputStream src, int vel, VehicleState vehState, VehicleDirection vDir, TrafficLight tl, ImageObserver imObs, Vehicle vAhead, int index) {
        vState = vehState;
        vehicleAhead = vAhead;
        vPrev = vState;                        //store previous state of vehicle
        vDirection = vDir;
        imgObserve = imObs;
        trafficLight = tl;
        command = Command.GO_STRAIGHT;
        try {
            mImage = ImageIO.read(src);
            //Random ran = new Random();
            //int turnBool;
            //Position Constants
            Vector2 RIGHT_LEFT_POS = new Vector2(1500, 265);
            Vector2 LEFT_RIGHT_POS = new Vector2(-300, 463);
            Vector2 DOWN_UP_POS = new Vector2(920, 840);
            Vector2 UP_DOWN_POS = new Vector2(520, -100);
            switch (index % 3) {
                case 0:
                    //right lane
                    //turnBool = ran.nextInt(5);
                    UP_DOWN_POS = new Vector2(UP_DOWN_POS.x + 100, UP_DOWN_POS.y);
                    DOWN_UP_POS = new Vector2(DOWN_UP_POS.x + 100, DOWN_UP_POS.y);
                    LEFT_RIGHT_POS = new Vector2(LEFT_RIGHT_POS.x, LEFT_RIGHT_POS.y + 70);
                    RIGHT_LEFT_POS = new Vector2(RIGHT_LEFT_POS.x, RIGHT_LEFT_POS.y + 60);
                    command = Command.TURN_RIGHT;
                    break;
                case 1:

                    break;
                case 2:
                    //left lane
                    //turnBool = ran.nextInt(10);
                    UP_DOWN_POS = new Vector2(UP_DOWN_POS.x - 120, UP_DOWN_POS.y);
                    DOWN_UP_POS = new Vector2(DOWN_UP_POS.x - 100, DOWN_UP_POS.y);
                    LEFT_RIGHT_POS = new Vector2(LEFT_RIGHT_POS.x, LEFT_RIGHT_POS.y - 70);
                    RIGHT_LEFT_POS = new Vector2(RIGHT_LEFT_POS.x, RIGHT_LEFT_POS.y - 70);
                    //if(turnBool%3 == 0)
                    command = Command.TURN_LEFT;
                    break;
            }

            //Set vehicle position according to the direction it's moving to
            //Junction Turning Constants
            Vector2 DOWN_LEFT = new Vector2(360, 0);
            Vector2 RIGHT_UP = new Vector2(0, 325);
            Vector2 RIGHT_DOWN = new Vector2(0, 454);
            Vector2 LEFT_UP = new Vector2(1000, 145);
            Vector2 UP_RIGHT = new Vector2(651, 593);
            Vector2 UP_LEFT = new Vector2(540, 600);
            switch (vDir) {
                case LEFT:
                    if (vehicleAhead != null && vehicleAhead.vehiclePosition.x > movingLeftPos) {
                        vehiclePosition = new Vector2(vehicleAhead.vehiclePosition.x + 300, RIGHT_LEFT_POS.y);
                    } else {
                        vehiclePosition = RIGHT_LEFT_POS;
                    }
                    break;

                case RIGHT:
                    if (vehicleAhead != null && vehicleAhead.vehiclePosition.x < movingRightPos) {
                        vehiclePosition = new Vector2(vehicleAhead.vehiclePosition.x - 300, LEFT_RIGHT_POS.y);
                    } else
                        vehiclePosition = LEFT_RIGHT_POS;
                    break;

                case UP:
                    if (vehicleAhead != null && vehicleAhead.vehiclePosition.y > movingUpPos) {
                        vehiclePosition = new Vector2(DOWN_UP_POS.x, vehicleAhead.vehiclePosition.y + 200);
                    } else
                        vehiclePosition = DOWN_UP_POS;
                    break;

                case DOWN:
                    if (vehicleAhead != null && vehicleAhead.vehiclePosition.y <= movingDownPos) {
                        vehiclePosition = new Vector2(UP_DOWN_POS.x, vehicleAhead.vehiclePosition.y - 200);
                    } else
                        vehiclePosition = UP_DOWN_POS;
                    break;
                case DOWN_LEFT:
                    vehiclePosition = DOWN_LEFT;
                    break;
                case RIGHT_UP:
                    vehiclePosition = RIGHT_UP;
                    break;
                case RIGHT_DOWN:
                    vehiclePosition = RIGHT_DOWN;
                    break;
                case LEFT_UP:
                    vehiclePosition = LEFT_UP;
                    break;
                case UP_LEFT:
                    vehiclePosition = UP_LEFT;
                    break;
                case UP_RIGHT:
                    vehiclePosition = UP_RIGHT;
                    break;
                default:
                    break;
            }

            trans = new AffineTransform();
            velocity = vel;
            vehVelo = velocity;
            mCurAngle = 0;
            trans.setToTranslation(vehiclePosition.x, vehiclePosition.y);

            if (vState == VehicleState.MOVE_Y) {
                mCurAngle = 90;
                trans.setToTranslation(vehiclePosition.x, vehiclePosition.y);
                trans.rotate(Math.toRadians(mCurAngle), (double) mImage.getWidth(imgObserve) / 2, (double) mImage.getHeight(imgObserve) / 2);
            }

            Timer mTimer = new Timer(10, this);
            mTimer.start();


        } catch (IOException e) {
            System.err.println("Can't find image");
        }
    }

    /**
     * Getter for vehicle image
     *
     * @return Image The vehicle image object
     */

    public Image getImage() {
        return mImage;
    }

    public float getSpeed() {
        return velocity;
    }

    public Vector2 getVehiclePosition() {
        return vehiclePosition;
    }


    public AffineTransform getTrans() {
        return trans;
    }

    public boolean isInView() {
        if (vDirection == VehicleDirection.LEFT && vehiclePosition.x < -100) {
            return false;
        }
        if (vDirection == VehicleDirection.RIGHT && vehiclePosition.x > 1500) {
            return false;
        }
        if (vDirection == VehicleDirection.UP && vehiclePosition.y < -100) {
            return false;
        }
        return vDirection != VehicleDirection.DOWN || vehiclePosition.y <= 850;
    }

    private void accelerate(int current_pos, int final_pos) {
        float dist = final_pos - current_pos;
        float t = dist / velocity;

        if (Math.abs(t) > 0)
            acceleration = (0 - velocity) / t;
        else
            acceleration = 0;

    }

    /**
     * @param angle the final angle you want the car to positioned at relative to the normal
     * @param time  the time in seconds you want the car to take to position itself at "angle"
     */
    private void steerTowards(float angle, float time) {
        //first we calculate the angular velocity required to get the vehicle to angle in time t
        float angularVel = angle / time;
        if (angle == 0 || angle == 90 || angle == 270) {
            angularVel = (angle - mCurAngle) / time;
        }
        //if(Math.abs(mCurAngle) < Math.abs(angle))
        mCurAngle += angularVel;

        trans.rotate(Math.toRadians(mCurAngle), mImage.getWidth(imgObserve), mImage.getHeight(imgObserve));
    }


    public void actionPerformed(ActionEvent e) {
        if (vehicleAhead != null && !vehicleAhead.isInView()) {
            vehicleAhead = null;
        }

        if (trafficLight != null && (trafficLight.forwardGo || passedTrafficLight) && !turn) {
            vState = vPrev;
        } else if (trafficLight != null && !trafficLight.forwardGo) {
            vState = VehicleState.BRAKE;
        }

        switch (vState) {
            case BRAKE:
                brake();
                break;
            case TURNING:
                turning();
                break;
            case MOVE_X:
            case MOVE_Y:
                move();
                break;
        }
    }

    private void move() {
        velocity = vehVelo;
        switch (vDirection) {
            case UP -> {
                //In any case in which our vehicleAhead has changed states, we forget that vehicle
                if (vehicleAhead != null && vehicleAhead.vState != this.vState) {
                    velocity = vehicleAhead.velocity;
                    vehicleAhead = null;

                }
                if (!passedTrafficLight && !trafficLight.forwardGo) {
                    vState = VehicleState.BRAKE;

                }
                if (vehiclePosition.y < movingUpPos) {
                    passedTrafficLight = true;
                    if (command == Command.TURN_RIGHT) {
                        vDirection = VehicleDirection.UP_RIGHT;
                        vState = VehicleState.TURNING;
                        turn = true;
                    }
                }
                if (velocity > 0) {        //Moving down
                    velocity *= -1;
                    //acceleration *= -1;
                    mCurAngle = -90;
                    //velocity += acceleration;
                }
                if (vehicleAhead != null) {
                    accelerate(vehiclePosition.y, vehicleAhead.vehiclePosition.y + 130);
                    velocity += acceleration;
                }
            }
            case DOWN -> {
                if (!passedTrafficLight && !trafficLight.forwardGo)
                    vState = VehicleState.BRAKE;

                if (vehiclePosition.y > movingDownPos) {
                    passedTrafficLight = true;

                    if (command == Command.TURN_LEFT) {
                        vState = VehicleState.TURNING;
                        turn = true;
                    }
                }
                if (velocity < 0) {
                    mCurAngle = 90;
                    velocity *= -1;
                }
                if (vehicleAhead != null) {
                    accelerate(vehiclePosition.y, vehicleAhead.vehiclePosition.y - 150);
                    if (acceleration < 0) {
                        acceleration = 0;
                        velocity = vehicleAhead.getSpeed();
                    }
                    acceleration *= -1;
                    velocity += acceleration;
                }

                if (vehicleAhead != null && vehicleAhead.vState != this.vState) {
                    velocity = vehicleAhead.velocity;
                    vehicleAhead = null;
                }
            }
        }

        switch (vDirection) {
            case LEFT -> {
                if (!passedTrafficLight && !trafficLight.forwardGo)
                    vState = VehicleState.BRAKE;

                if (vehiclePosition.x < movingLeftPos && !turn) {
                    passedTrafficLight = true;
                    if (command == Command.TURN_LEFT) {
                        vDirection = VehicleDirection.LEFT_UP;
                        vState = VehicleState.TURNING;
                        turn = true;
                    }
                }
                if (velocity > 0) {
                    velocity *= -1;
                    mCurAngle = 180;
                }
                if (vehicleAhead != null) {
                    accelerate(vehiclePosition.x, vehicleAhead.vehiclePosition.x + 120);
                    if (acceleration > 0) {
                        acceleration = 0;
                        velocity = vehicleAhead.getSpeed();
                    }
                    velocity += acceleration;
                }
            }
            case RIGHT -> {
                if (!passedTrafficLight && !trafficLight.forwardGo)
                    vState = VehicleState.BRAKE;

                if (vehiclePosition.x > movingRightPos) {
                    passedTrafficLight = true;

                    if (command == Command.TURN_RIGHT) {
                        vDirection = VehicleDirection.RIGHT_DOWN;
                        vState = VehicleState.TURNING;
                        turn = true;
                    }
                }
                if (velocity < 0) {
                    velocity *= -1;
                    mCurAngle = 0;
                }
                if (vehicleAhead != null) {
                    if (acceleration > 0)
                        accelerate(vehiclePosition.x, vehicleAhead.vehiclePosition.x - 150);
                    else
                        acceleration = 0;
                    acceleration *= -1;
                    velocity += acceleration;
                }
            }
        }

        if (vDirection == VehicleDirection.UP || vDirection == VehicleDirection.DOWN) {
            this.vehiclePosition.y += (int) velocity;
        } else {
            this.vehiclePosition.x += (int) velocity;
        }
        this.trans.setToTranslation(vehiclePosition.x, vehiclePosition.y);
        this.trans.rotate(Math.toRadians(mCurAngle), (double) mImage.getWidth(imgObserve) / 2, (double) mImage.getHeight(imgObserve) / 2);
    }

    private void turning() {
        this.trafficLight = null;
        switch (command) {
            case TURN_LEFT -> {
                switch (vDirection) {
                    case DOWN:
                        if (mCurAngle == 180) {                        //if we reach the angle 180, we have to change the vehicle State
                            vDirection = VehicleDirection.LEFT;
                            vState = VehicleState.MOVE_X;

                        }

                        steerTowards(180, 40);
                        vehiclePosition.x -= (int) Math.abs(velocity - 2);
                        if (vehiclePosition.y <= movingDownPos + 80)
                            vehiclePosition.y += (int) Math.abs(velocity - 3);

                        //System.out.println("Vehicle ahead null: " + vehicleAhead);

                        this.trans.setToTranslation(vehiclePosition.x, vehiclePosition.y);
                        this.trans.rotate(Math.toRadians(mCurAngle), (double) mImage.getWidth(imgObserve) / 2, (double) mImage.getHeight(imgObserve) / 2);
                        break;

                    case LEFT_UP:
                        if (mCurAngle == 270) {                        //if we reach the angle 180, we have to change the vehicle State
                            vDirection = VehicleDirection.LEFT;
                            vState = VehicleState.MOVE_X;

                        }

                        steerTowards(270, 10);
                        vehiclePosition.y -= (int) Math.abs(velocity - 2);
                        if (vehiclePosition.x >= movingLeftPos - 100)
                            vehiclePosition.x -= 5;

                        //System.out.println("Vehicle ahead null: " + vehicleAhead);

                        this.trans.setToTranslation(vehiclePosition.x, vehiclePosition.y);
                        this.trans.rotate(Math.toRadians(mCurAngle), (double) mImage.getWidth(imgObserve) / 2, (double) mImage.getHeight(imgObserve) / 2);
                        break;
                    default:
                        break;

                }
                //System.out.println("Should be turning");
            }
            case TURN_RIGHT -> {
                switch (vDirection) {
                    case UP_RIGHT:
                        if (mCurAngle == 0) {                        //if we reach the angle 180, we have to change the vehicle State
                            vDirection = VehicleDirection.RIGHT;
                            vState = VehicleState.MOVE_X;

                        }
                        steerTowards(0, 9);

                        vehiclePosition.x += 5;
                        if (vehiclePosition.y >= movingUpPos - 120)
                            vehiclePosition.y -= 6;

                        this.trans.setToTranslation(vehiclePosition.x, vehiclePosition.y);
                        this.trans.rotate(Math.toRadians(mCurAngle), (double) mImage.getWidth(imgObserve) / 2, (double) mImage.getHeight(imgObserve) / 2);
                        break;

                    case RIGHT_DOWN:
                        if (mCurAngle == 90) {                        //if we reach the angle 180, we have to change the vehicle State
                            vDirection = VehicleDirection.DOWN;
                            vState = VehicleState.MOVE_Y;

                        }
                        steerTowards(90, 9);

                        if (vehiclePosition.x <= movingRightPos + 50) {
                            vehiclePosition.x += 2;
                        }
                        vehiclePosition.y += 5;

                        this.trans.setToTranslation(vehiclePosition.x, vehiclePosition.y);
                        this.trans.rotate(Math.toRadians(mCurAngle), (double) mImage.getWidth(imgObserve) / 2, (double) mImage.getHeight(imgObserve) / 2);
                        break;
                    default:
                        break;
                }
            }
            default -> {
            }
        }
    }

    private void brake() {
        switch (vDirection) {
            case LEFT:
                if (velocity > 0) {
                    velocity *= -1;
                    mCurAngle = 180;
                }
                if (vehicleAhead == null) {
                    accelerate(vehiclePosition.x, movingLeftPos);
                    velocity += acceleration;
                    if (vehiclePosition.x >= movingLeftPos) {

                        this.vehiclePosition.x += (int) velocity;
                    }
                } else {
                    if (vehicleAhead.vehiclePosition.x < 1450 && vehicleAhead.vehiclePosition.x > movingLeftPos) {
                        accelerate(vehiclePosition.x, vehicleAhead.getVehiclePosition().x + 120);
                        if (vehiclePosition.x >= vehicleAhead.vehiclePosition.x + 120) {
                            velocity += acceleration;
                            this.vehiclePosition.x += (int) velocity;
                        }
                    }

                }
                break;
            case RIGHT:

                if (vehicleAhead != null) {
                    if ((vehicleAhead.vehiclePosition.x > -50 && vehicleAhead.vehiclePosition.x < movingRightPos)) {
                        accelerate(vehiclePosition.x, vehicleAhead.getVehiclePosition().x - 150);

                        if (acceleration > 0)
                            acceleration *= -1;

                        velocity += acceleration;
                        if (vehiclePosition.x <= vehicleAhead.getVehiclePosition().x - 150) {
                            this.vehiclePosition.x += (int) velocity;
                        }

                    }
                } else {
                    accelerate(vehiclePosition.x, movingRightPos);
                    velocity += acceleration;
                    if (vehiclePosition.x <= movingRightPos)
                        this.vehiclePosition.x += (int) velocity;
                }
                break;

            case UP:
                mCurAngle = -90;
                if (velocity > 0) {
                    velocity *= -1;
                }

                if (vehicleAhead != null && vehicleAhead.passedTrafficLight)
                    vehicleAhead = null;

                if (vehicleAhead == null) {
                    accelerate(vehiclePosition.y, movingUpPos);
                    velocity += acceleration;
                    if (vehiclePosition.y >= movingUpPos) {
                        this.vehiclePosition.y += (int) velocity;
                    }
                } else {
                    if (vehicleAhead.vehiclePosition.y > 650 && vehicleAhead.vehiclePosition.y > movingUpPos) {
                        accelerate(vehiclePosition.y, vehicleAhead.vehiclePosition.y + 150);
                        velocity += acceleration;
                        if (vehiclePosition.y > vehicleAhead.vehiclePosition.y + 150) {

                            this.vehiclePosition.y += (int) velocity;
                        }
                    }
                }
                break;
            case DOWN:
                if (vehicleAhead != null && vehicleAhead.passedTrafficLight)
                    vehicleAhead = null;

                if (acceleration > 0)        //new
                    acceleration *= -1;

                if (vehicleAhead == null) {
                    accelerate(vehiclePosition.y, movingDownPos);
                    velocity += acceleration;
                    if (vehiclePosition.y <= movingDownPos) {

                        this.vehiclePosition.y += (int) velocity;
                    }

                } else {

                    if (vehicleAhead.vehiclePosition.y > -50 && vehicleAhead.vehiclePosition.y < movingDownPos) {
                        //if(vehicleAhead.vehiclePosition.y > vehiclePosition.y)
                        accelerate(vehiclePosition.y, vehicleAhead.vehiclePosition.y - 140);
                        velocity += acceleration;
                        if (vehiclePosition.y <= vehicleAhead.vehiclePosition.y - 140) {

                            this.vehiclePosition.y += (int) velocity;
                        }
                    }
                }

                break;
            default:
                break;
        }

        this.trans.setToTranslation(vehiclePosition.x, vehiclePosition.y);
        this.trans.rotate(Math.toRadians(mCurAngle), (double) mImage.getWidth(imgObserve) / 2, (double) mImage.getHeight(imgObserve) / 2);
        //nothing now
    }

}