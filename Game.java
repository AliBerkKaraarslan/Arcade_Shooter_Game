//************************************************************************************
// Game.java         Author:Ali Berk Karaarslan     Date:25.04.2023
//
// Arcade Shooter Game project.
// To play it, create AirCraft, Enemy and Friend Objects.(AirCraft is the main player.There can only be one.
// But there could be multiple Enemy and Friend objects) After that, start all the objects (ex: aircraft.start();)
// Game is played with WASD keys and Mouse Buttons.
//************************************************************************************

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Random;

public class Game extends JFrame {

    MainPanel panel;

    //Refresh time of the screen
    private double refreshTime = 0.05;

    //These parameters in the type of second
    private double bulletMoveWaitTime = 0.1;
    private double characterMoveWaitTime = 0.5;
    private double characterShootWaitTime = 1;

    //These parameters in the type of pixels
    private int aircraftSize = 10;
    private int enemySize = 10;
    private int friendSize = 10;
    private int bulletSize = 5;

    private ArrayList<Character> characters = new ArrayList<>();
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Friend> friends = new ArrayList<>();
    private ArrayList<Bullet> bullets = new ArrayList<>();

    private AirCraft aircraft = new AirCraft();

    //These garbage avoid the ConcurrentModificationException
    //Basically when an elements need to be removed, It transferred to correspond garbage ArrayList.
    //In every refresh time, These items removed from the main ArrayList
    private boolean thereIsGarbageEnemy = false;
    private ArrayList<Enemy> garbageEnemy = new ArrayList<>();

    private boolean thereIsGarbageFriend = false;
    private ArrayList<Friend> garbageFriend = new ArrayList<>();

    private boolean thereIsGarbageBullet = false;
    private ArrayList<Bullet> garbageBullet = new ArrayList<>();

    Color customPurple = new Color(211, 40, 255);

    GameOverFrame gameOver = null;

    public Game() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        panel = new MainPanel();
        add(panel);

        pack();
        setLocationRelativeTo(null);

        Thread refresher = new Thread(new RefresherThread());
        refresher.start();

        setVisible(true);
    }

    //GameOverFrame. Pops up win or lose frame
    class GameOverFrame extends JFrame {

        public GameOverFrame(boolean win) {

            setSize(200, 150);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            JLabel gameOver;

            //If user wins the game
            if (win)
                gameOver = new JLabel("You Won", JLabel.CENTER);

            //If user loses the game
            else
                gameOver = new JLabel("Game Over", JLabel.CENTER);

            add(gameOver);
            setVisible(true);
        }
    }

    //MainPanel of the program. Paints all the elements
    class MainPanel extends JPanel {

        public MainPanel(){
            setPreferredSize(new Dimension(500,500));
        }

        public void paint(Graphics g) {
            //Calls all the paint methods
            super.paint(g);
            paintBullets(g);
            paintEnemies(g);
            paintFriends(g);
            paintAirCraft(g);

            //Drawing grids
            /*
            for(int i=0;i<=500;i+=10) {
                g.setColor(Color.BLACK);
                g.drawLine(0, i, 500, i);
                g.drawLine(i, 0, i, 500);
            }
             */
        }

        //Paints the aircraft
        public void paintAirCraft(Graphics g) {
            g.setColor(aircraft.color);
            g.fillRect(aircraft.positionX, aircraft.positionY, aircraft.size, aircraft.size);
        }

        //Clears the enemies garbage and then paints all the enemies
        public void paintEnemies(Graphics g) {
            synchronized (aircraft) {

                //If there is garbage then removes it from the enemies and characters ArrayList. Then clears the garbage
                if (thereIsGarbageEnemy) {
                    enemies.removeAll(garbageEnemy);
                    characters.removeAll(garbageEnemy);
                    thereIsGarbageEnemy = false;
                    garbageEnemy.clear();
                }

                //Starts from the end of the ArrayList to avoid ConcurrentModificationException
                for (int i = enemies.size() - 1; i >= 0; i--) {
                    if (checkExist(enemies.get(i))) {
                        g.setColor(enemies.get(i).color);
                        g.fillRect(enemies.get(i).positionX, enemies.get(i).positionY, enemies.get(i).size, enemies.get(i).size);
                    }
                }
            }
        }

        //Clears the friends garbage and then paints all the friends
        public void paintFriends(Graphics g) {
            synchronized (aircraft) {

                //If there is garbage then removes it from the friends and characters ArrayList. Then clears the garbage
                if (thereIsGarbageFriend) {
                    friends.removeAll(garbageFriend);
                    characters.removeAll(garbageFriend);
                    thereIsGarbageFriend = false;
                    garbageFriend.clear();
                }

                //Starts from the end of the ArrayList to avoid ConcurrentModificationException
                for (int i = friends.size() - 1; i >= 0; i--) {
                    if (checkExist(friends.get(i))) {
                        g.setColor(friends.get(i).color);
                        g.fillRect(friends.get(i).positionX, friends.get(i).positionY, friends.get(i).size, friends.get(i).size);
                    }
                }
            }
        }

        //Clears the bullets garbage and then paints all the bullets
        public void paintBullets(Graphics g) {
            synchronized (aircraft) {

                //If there is garbage then removes it from the bullets ArrayList. Then clears the garbage
                if (thereIsGarbageBullet) {
                    bullets.removeAll(garbageBullet);
                    thereIsGarbageBullet = false;
                    garbageBullet.clear();
                }

                //Starts from the end of the ArrayList to avoid ConcurrentModificationException exception
                for (int i = bullets.size() - 1; i >= 0; i--) {
                    if (checkExist(bullets.get(i))) {
                        g.setColor(bullets.get(i).color);
                        g.fillRect(bullets.get(i).positionX, bullets.get(i).positionY, bullets.get(i).size, bullets.get(i).size);
                    }
                }
            }
        }
    }

    //Refreshes the program in every period (refreshTime) by calling the MainPanel's repaint method.
    class RefresherThread extends Thread {
        public void run() {

            while (checkExist(aircraft)) {
                try {
                    Thread.sleep((long) (refreshTime * 1000));
                    panel.repaint();
                } catch (InterruptedException e) {
                    //throw new RuntimeException(e);
                }
            }
        }
    }

    //Parent class of the AirCraft, Enemy and Friend classes.
    class Character extends Thread {
        int positionX;
        int positionY;
        int size;
        Color color;
        boolean alive = true;
    }

    //Main player of the program. Extends Character class.
    class AirCraft extends Character implements KeyListener, MouseInputListener {

        public AirCraft() {
            size = aircraftSize;
            color = Color.RED;

            aircraft = this;
            characters.add(this);

            //These inital coordinates avoid a bug
            positionX = -100;
            positionY = -100;
        }

        public void run() {
            //Start coordinates of the AirCraft
            positionX = 250;
            positionY = 250;
            addKeyListener(this);
            addMouseListener(this);
        }

        public void deleteAirCraft(Boolean win) {
            alive = false;
            dispose();
            //If there is no gameOver frame then creates one
            if (gameOver == null)
                gameOver = new GameOverFrame(win);
        }

        //Checks if WASD keys have pressed. If yes then changes the position of the aircraft
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            //User pressed the W key. Moves the aircraft 10 pixel up (If it is in the frame)
            if (key == KeyEvent.VK_W)
                if (positionY >= 10)
                    positionY -= 10;

            //User pressed the A key. Moves the aircraft 10 pixel left (If it is in the frame)
            if (key == KeyEvent.VK_A)
                if (positionX >= 10)
                    positionX -= 10;

            //User pressed the S key. Moves the aircraft 10 pixel down (If it is in the frame)
            if (key == KeyEvent.VK_S)
                if (positionY <= 480)
                    positionY += 10;

            //User pressed the D key. Moves the aircraft 10 pixel right (If it is in the frame)
            if (key == KeyEvent.VK_D)
                if (positionX <= 480)
                    positionX += 10;

            panel.repaint();
            checkCharacterCollision(this);
        }

        //If user pressed mouse button then creates two bullets. One goes left and other one goes right
        @Override
        public void mousePressed(MouseEvent e) {
            AirCraftBullet bulletLeft = new AirCraftBullet(positionX - bulletSize, positionY, -1);
            AirCraftBullet bulletRight = new AirCraftBullet(positionX + size, positionY, 1);

            //Adding these bullets to bullets ArrayList
            bullets.add(bulletLeft);
            bullets.add(bulletRight);
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
        }

        public void mouseMoved(MouseEvent e) {
        }
    }

    //Enemies of the program. Extends Character class.
    class Enemy extends Character {

        public Enemy() {
            size = enemySize;
            color = Color.BLACK;

            //Randomly selects coordinates
            Random generator = new Random();
            int possibleX = (generator.nextInt(500) / 10) * 10;
            int possibleY = (generator.nextInt(500) / 10) * 10;

            //Checks if the coordinates is empty (Looks all the characters' positions)
            for (Character character : characters) {

                //Continues the process until finds empty coordinates
                while ((character.positionX == possibleX && character.positionY == possibleY ) || (possibleX==250 && possibleY==250)) {
                    possibleX = (generator.nextInt(500) / 10) * 10;
                    possibleY = (generator.nextInt(500) / 10) * 10;

                }
            }

            //Assigning these empty coordinates
            positionX = possibleX;
            positionY = possibleY;

            //Adding this new enemy to enemies and characters ArrayList
            characters.add(this);
            enemies.add(this);
        }

        public void run() {
            //Starts the Move and Shoot events (Starts the threads)
            if (checkExist(this)) {
                new Move(this).start();
                new Shoot(this).start();
            }
        }

        //Removes the enemy from the ArrayLists (Adds to the enemy garbage)
        public void deleteEnemy() {
            alive = false;
            garbageEnemy.add(this);
            thereIsGarbageEnemy = true;
            //Checks if all the enemies removed
            checkWin();
        }
    }

    //Friends of the AirCraft(player). Extends Character class.
    class Friend extends Character {

        public Friend() {
            size = friendSize;
            color = Color.GREEN;

            //Randomly selects coordinates
            Random generator = new Random();
            int possibleX = (generator.nextInt(500) / 10) * 10;
            int possibleY = (generator.nextInt(500) / 10) * 10;

            //Checks if the coordinates is empty (Looks all the characters' positions)
            for (Character character : characters) {

                //Continues the process until finds empty coordinates
                while ((character.positionX == possibleX && character.positionY == possibleY ) || (possibleX==250 && possibleY==250)) {
                    possibleX = (generator.nextInt(500) / 10) * 10;
                    possibleY = (generator.nextInt(500) / 10) * 10;
                }
            }

            //Assigning these empty coordinates
            positionX = possibleX;
            positionY = possibleY;

            //Adding this new friend to friends and characters ArrayList
            characters.add(this);
            friends.add(this);
        }

        public void run() {
            //Starts the Move and Shoot events (Starts the threads)
            if (checkExist(this)) {
                new Move(this).start();
                new Shoot(this).start();
            }
        }

        //Removes the friend from the ArrayLists (Adds to the friend garbage)
        public void deleteFriend() {
            alive = false;
            garbageFriend.add(this);
            thereIsGarbageFriend = true;
        }
    }

    //Moves the given character in a random direction every period
    class Move extends Thread {

        //Waits waitTime then moves
        double waitTime = characterMoveWaitTime;
        Character character;

        public Move(){}
        public Move(Character character) {
            this.character = character;
        }

        public void run() {

            //Works until player loses the game
            while (checkExist(aircraft)) {
                try {
                    Thread.sleep((long) (waitTime * 1000));
                    moveOnce(character);
                    checkCharacterCollision(character);

                } catch (InterruptedException e) {
                    //throw new RuntimeException(e);
                }
            }
        }

        //Moves the given character in random direction once
        public void moveOnce(Character character){
            Random generator = new Random();

            //Randomly selects a direction
            //0->North   1->West   2->South   3->East
            int direction = generator.nextInt(4);

            //Moves the character 10 pixel up (If it is in the frame)
            if (direction == 0)
                if (character.positionY >= 10)
                    character.positionY -= 10;

            //Moves the character 10 pixel left (If it is in the frame)
            if (direction == 1)
                if (character.positionX >= 10)
                    character.positionX -= 10;

            //Moves the character 10 pixel down (If it is in the frame)
            if (direction == 2)
                if (character.positionY <= 480)
                    character.positionY += 10;

            //Moves the character 10 pixel right (If it is in the frame)
            if (direction == 3)
                if (character.positionX <= 480)
                    character.positionX += 10;

            checkCharacterCollision(character);
        }
    }

    //Shoots the bullets of the given character in every period
    class Shoot extends Thread {

        //Waits waitTime and shoots
        double waitTime = characterShootWaitTime;
        Character character;

        public Shoot(Character character) {
            this.character = character;
        }

        public void run() {

            //Works until player loses the game
            while (checkExist(character)) {
                try {
                    Thread.sleep((long) (waitTime * 1005));

                    if (checkExist(character)) {
                        //Shooting the enemy's bullets
                        if (character instanceof Enemy) {
                            EnemyBullet bulletLeft = new EnemyBullet(character.positionX - bulletSize, character.positionY, -1);
                            EnemyBullet bulletRight = new EnemyBullet(character.positionX + 10, character.positionY, 1);
                            bullets.add(bulletLeft);
                            bullets.add(bulletRight);
                        }
                        //Shooting the friend's bullets
                        else if (character instanceof Friend) {
                            FriendBullet bulletLeft = new FriendBullet(character.positionX - bulletSize, character.positionY, -1);
                            FriendBullet bulletRight = new FriendBullet(character.positionX + character.size, character.positionY, 1);
                            bullets.add(bulletLeft);
                            bullets.add(bulletRight);
                        }
                    }
                } catch (InterruptedException e) {
                    //throw new RuntimeException(e);
                }
            }
        }
    }

    //Parent class of the AirCraftBullet, EnemyBullet and FriendBullet classes.
    class Bullet extends Thread {
        Color color;
        int positionX;
        int positionY;
        int size = bulletSize;
        double movementTime = bulletMoveWaitTime;
        Boolean alive = true;

        //-1 or +1. -1 represents left ,+1 represents right
        int type;

        public void run() {
            try {

                //Every period moves the bullet(10 px) and checks collision
                while (isValid()) {
                    Thread.sleep((long) (movementTime * 1000));
                    checkCollision();
                    positionX += 10 * type;
                }

            } catch (InterruptedException e) {
                //throw new RuntimeException(e);
            } finally {
                deleteBullet();
            }
        }

        //Checks if the bullet is in the frame.
        public boolean isValid() {
            if (positionX <= 514 - size && positionX >= 0) {
                if (positionY <= 537 - size && positionY >= 0) {
                    if (checkExist(this))
                        return true;
                }
            }
            return false;
        }

        //Removes the enemy from the ArrayLists (Adds to the enemy garbage)
        public void deleteBullet() {
            alive = false;
            garbageBullet.add(this);
            thereIsGarbageBullet = true;
        }

        //Checks the bullet collision with characters
        public void checkCollision() {
        }
    }

    //AirCraft's Bullet. Extends Bullet class.
    class AirCraftBullet extends Bullet {

        public AirCraftBullet(int positionX, int positionY, int type) {
            color = Color.ORANGE;
            this.positionX = positionX;
            this.positionY = positionY;
            this.type = type;
            start();
        }

        //Checks all the characters. If it has collision with bullet then makes corresponded process
        @Override
        public void checkCollision() {
            try {
                for (Character curr : characters) {

                    //Checks if they exits
                    if (checkExist(curr) && checkExist(this)) {

                        //Checks if they have collision
                        if (curr.positionX <= positionX && curr.positionX + curr.size > positionX) {
                            if (curr.positionY <= positionY && curr.positionY + curr.size > positionY) {

                                //If the character is Enemy. Then deletes the enemy and itself (bullet)
                                if (curr instanceof Enemy) {
                                    deleteBullet();
                                    ((Enemy) curr).deleteEnemy();

                                }//If the character is Friend or AirCraft. Then deletes itself (bullet)
                                else if (curr instanceof Friend || curr instanceof AirCraft) {
                                    deleteBullet();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                //throw new RuntimeException(e);
            }
        }
    }

    //Enemy's Bullet. Extends Bullet class.
    class EnemyBullet extends Bullet {

        public EnemyBullet(int positionX, int positionY, int type) {
            color = Color.BLUE;
            this.positionX = positionX;
            this.positionY = positionY;
            this.type = type;
            start();
        }

        //Checks all the characters. If it has collision with bullet then makes corresponded process
        @Override
        public void checkCollision() {
            try {
                for (Character curr : characters) {

                    //Checks if they exits
                    if (checkExist(curr) && checkExist(this)) {

                        //Checks if they have collision
                        if (curr.positionX <= positionX && curr.positionX + curr.size > positionX) {
                            if (curr.positionY <= positionY && curr.positionY + curr.size > positionY) {

                                //If the character is Friend. Then deletes the Friend and itself (bullet)
                                if (curr instanceof Friend) {
                                    deleteBullet();
                                    ((Friend) curr).deleteFriend();

                                }//If the character is AirCraft. Then deletes the AirCraft and itself (bullet)
                                else if (curr instanceof AirCraft) {
                                    deleteBullet();
                                    aircraft.deleteAirCraft(false);

                                }//If the character is Enemy. Then deletes itself (bullet)
                                else if (curr instanceof Enemy) {
                                    deleteBullet();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                //throw new RuntimeException(e);
            }
        }
    }

    //Friend's Bullet. Extends Bullet class.
    class FriendBullet extends Bullet {

        public FriendBullet(int positionX, int positionY, int type) {
            color = customPurple;
            this.positionX = positionX;
            this.positionY = positionY;
            this.type = type;
            start();
        }

        //Checks all the characters. If it has collision with bullet then makes corresponded process
        @Override
        public void checkCollision() {
            try {
                for (Character curr : characters) {

                    //Checks if they exits
                    if (checkExist(curr) && checkExist(this)) {

                        //Checks if they have collision
                        if (curr.positionX <= positionX && curr.positionX + curr.size > positionX) {
                            if (curr.positionY <= positionY && curr.positionY + curr.size > positionY) {

                                //If the character is Enemy. Then deletes the Enemy and itself (bullet)
                                if (curr instanceof Enemy) {
                                    deleteBullet();
                                    ((Enemy) curr).deleteEnemy();

                                }//If the character is Friend or AirCraft. Then deletes itself (bullet)
                                else if (curr instanceof Friend || curr instanceof AirCraft) {
                                    deleteBullet();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                //throw new RuntimeException(e);
            }
        }
    }

    //Checks if the given object (Character or Fire)
    public boolean checkExist(Object object) {

        //Checks if the object is null (avoid NullPointerException)
        if (object != null) {
            //Object is Character
            if (object instanceof Character)
                return ((Character) object).alive;

                //Object is Bullet
            else if (object instanceof Bullet)
                return ((Bullet) object).alive;
        }
        return false;
    }

    //Checks if there is any enemy left. If yes then returns false
    public boolean checkWin() {

        //Checks if game is still going
        if (checkExist(aircraft)) {
            synchronized (aircraft) {

                //Checks the enemies ArrayList. If there is an enemy then returns false
                for (int i = enemies.size() - 1; i >= 0; i--) {
                    if (checkExist(enemies.get(i))) {
                        return false;
                    }
                }

                //If there is no enemies left then ends the game with win (deletes aircraft)
                aircraft.deleteAirCraft(true);
                return true;
            }
        }
        return false;
    }

    //Checks the characters collision
    public void checkCharacterCollision(Character character) {
        synchronized (aircraft) {

            //Checks all the characters
            for (Character curr : characters) {

                //Skips itself
                if (!curr.equals(character)) {

                    //If they exits
                    if (checkExist(curr) && checkExist(character)) {

                        //If they have collision
                        if (curr.positionX <= character.positionX && curr.positionX + curr.size > character.positionX) {
                            if (curr.positionY <= character.positionY && curr.positionY + curr.size > character.positionY) {

                                //If Enemy and Friend has collision. Then deletes them both
                                if (curr instanceof Friend && character instanceof Enemy) {
                                    ((Friend) curr).deleteFriend();
                                    ((Enemy) character).deleteEnemy();
                                }
                                //If Enemy and AirCraft has collision. Then ends the game with lose (deletes aircraft)
                                else if (curr instanceof Enemy && character instanceof AirCraft) {
                                    aircraft.deleteAirCraft(false);
                                }
                                else if (curr instanceof AirCraft && character instanceof Enemy) {
                                    aircraft.deleteAirCraft(false);
                                }

                                //If Enemy and Enemy has collision. Then moves one of them in random direction
                                else if (curr instanceof Enemy && character instanceof Enemy) {
                                    new Move().moveOnce(curr);
                                }
                                //If Friend and Friend has collision. Then moves one of them in random direction
                                else if (curr instanceof Friend && character instanceof Friend) {
                                    new Move().moveOnce(curr);
                                }
                                //If Friend and AirCraft has collision. Then moves Friend in random direction
                                else if (curr instanceof Friend && character instanceof AirCraft) {
                                    new Move().moveOnce(curr);
                                }
                                else if (curr instanceof AirCraft && character instanceof Friend) {
                                    new Move().moveOnce(character);
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}
