package h1Pacman;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

//imports for highscore
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Date; 

import javax.swing.ImageIcon;
//import for highscore
import javax.swing.JOptionPane;
// ---
import javax.swing.JPanel;
import javax.swing.Timer;

//imports for sounds
import sf.Sound;
import sf.SoundFactory;


@SuppressWarnings("serial")
public class Board extends JPanel implements ActionListener {
  
  //sounds
  public final static String DIR = "src/res/";
  public final static String SOUND_CHOMP = DIR + "pacman_chomp.wav";
  public final static String SOUND_DEATH = DIR + "pacman_death.wav";
  public final static String SOUND_EATGHOST = DIR + "pacman_eatghost.wav";
  public final static String SOUND_POWERUP = DIR + "pacman_eatfruit.wav";
	
  private String highscore = "";
  
  Dimension d;
  Font smallfont = new Font("Helvetica", Font.BOLD, 20);

  FontMetrics fmsmall, fmlarge;
  Image ii;
  Color dotcolor = new Color(192, 192, 0);
  Color pelletcolor = new Color(255,255,255);
  Color mazecolor;

  boolean ingame = false;
  boolean dying = false;
  boolean ppellet = false; 

  final int blocksize = 48;
  final int nrofblocks = 15;
  final int scrsize = nrofblocks * blocksize;
  final int pacanimdelay = 2;
  final int pacmananimcount = 4;
  final int maxghosts = 12;
  final int pacmanspeed = 6;

  int pacanimcount = pacanimdelay;
  int pacanimdir = 1;
  int pacmananimpos = 0;
  int nrofghosts = 6;
  int pacsleft, score;
  int deathcounter;
  int[] dx, dy;
  int[] ghostx, ghosty, ghostdx, ghostdy, ghostspeed;
  int cherriesx, cherriesy;
	
  int level; 

  Image ghost;
  Image pacman1, pacman2up, pacman2left, pacman2right, pacman2down;
  Image pacman3up, pacman3down, pacman3left, pacman3right;
  Image pacman4up, pacman4down, pacman4left, pacman4right;
  Image cherries;
  Image pellghost;
	
  public enum State {NONE, START, NEXT, RESTART, DONE}; 
  private State state = State.NONE;

  int pacmanx, pacmany, pacmandx, pacmandy;
  int reqdx, reqdy, viewdx, viewdy;

  // Power pellet #'s are
  // 35 top left
  // 41 bottom left
  // 38 top right
  // 44 bottom right
	
  // 1 left
  // 2 right
  // 4 top 
  // 8 bottom
	
  // levels
  final short leveldata[] [] =
    { {35, 26, 26, 26, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 38,
        21, 0,  0,  0,  17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        21, 0,  0,  0,  17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20, 
        21, 0,  0,  0,  17, 16, 16, 24, 16, 16, 16, 16, 16, 16, 20, 
        17, 18, 18, 18, 16, 16, 20, 0,  17, 16, 16, 16, 16, 16, 20,
        17, 16, 16, 16, 16, 16, 20, 0,  17, 16, 16, 16, 16, 24, 20, 
        25, 16, 16, 16, 24, 24, 28, 0,  25, 24, 24, 16, 20, 0,  21, 
        1,  17, 16, 20, 0,  0,  0,  0,  0,  0,  0,  17, 20, 0,  21,
        1,  17, 16, 16, 18, 18, 22, 0,  19, 18, 18, 16, 20, 0,  21,
        1,  17, 16, 16, 16, 16, 20, 0,  17, 16, 16, 16, 20, 0,  21, 
        1,  17, 16, 16, 16, 16, 20, 0,  17, 16, 16, 64, 20, 0,  21,
        1,  17, 16, 16, 16, 16, 16, 18, 16, 16, 16, 16, 20, 0,  21,
        1,  17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  21,
        1,  41, 24, 24, 24, 24, 24, 24, 24, 24, 16, 16, 16, 18, 20,
        9,  8,  8,  8,  8,  8,  8,  8,  8,  8,  25, 24, 24, 24, 44 },

  // level two
     
     {35, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 38,
      17, 16, 24, 16, 16, 24, 16, 16, 24, 24, 24, 24, 24, 16, 20,
      17, 20,  0, 17, 20,  0, 17, 20,  0,  0,  0,  0,  0, 17, 20,
      17, 20,  0, 17, 20,  0, 17, 16, 18, 22,  0, 19, 18, 16, 20,
      17, 20,  0, 17, 20,  0, 17, 16, 16, 20,  0, 17, 16, 16, 20,
      17, 20,  0, 17, 20,  0, 17, 16, 16, 20,  0, 17, 16, 16, 20,
      17, 20,  0, 25, 28,  0, 17, 16, 16, 20,  0, 17, 16, 16, 20,
      17, 20,  0,  0,  0,  0, 17, 16, 16, 20,  0, 17, 16, 16, 20,
      17, 20,  0, 19, 22,  0, 17, 64, 16, 20,  0, 17, 16, 16, 20,
      17, 20,  0, 17, 20,  0, 17, 16, 16, 20,  0, 17, 16, 16, 20,
      17, 20,  0, 17, 20,  0, 17, 16, 16, 20,  0, 17, 16, 16, 20,
      17, 20,  0, 17, 20,  0, 17, 16, 24, 28,  0, 25, 24, 16, 20,
      17, 20,  0, 17, 20,  0, 17, 20,  0,  0,  0,  0,  0, 17, 20,
      17, 16, 18, 16, 16, 18, 16, 16, 18, 18, 18, 18, 18, 16, 20,
      41, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 44 },
     
     // level three
     
     {35, 26, 26, 26, 18, 18, 18, 18, 18, 18, 18, 26, 18, 18, 38,
      21, 0,  0,  0,  17, 16, 16, 16, 16, 16, 20,  0, 17, 16, 20,
      21, 0,  0,  0,  17, 16, 16, 64, 16, 16, 20,  0, 17, 16, 20,
      21, 0,  0,  0,  17, 16, 24, 24, 24, 16, 20,  0, 17, 16, 20,
      17, 18, 18, 18, 16, 20,  0,  0,  0, 17, 20,  0, 17, 16, 20,
      17, 16, 16, 16, 16, 16, 22,  0, 19, 16, 20,  0, 17, 24, 20,
      25, 16, 16, 24, 16, 16, 20,  0, 17, 16, 20,  0, 21, 0,  21,
      1,  17, 20,  0, 17, 16, 16, 18, 16, 16, 20,  0, 21, 0,  21,
      1,  17, 20,  0, 17, 16, 16, 16, 16, 16, 16, 18, 20, 0,  21,
      1,  17, 20,  0, 17, 16, 16, 16, 16, 16, 16, 16, 20, 0,  21,
      1,  17, 20,  0, 17, 16, 16, 16, 16, 24, 24, 16, 20, 0,  21,
      1,  17, 16, 18, 16, 16, 16, 16, 20,  0,  0, 17, 20, 0,  21,
      1,  17, 16, 16, 16, 16, 16, 16, 20,  0,  0, 17, 20, 0,  21,
      1,  41, 24, 24, 24, 16, 16, 16, 16, 18, 18, 16, 16, 18, 20,
      9,  8,  8,  8,  8,  25, 24, 24, 24, 24, 24, 24, 24, 24, 44 },
     
     //level four
     
     {35, 26, 26, 26, 18, 18, 18, 18, 18, 18, 18, 18, 26, 18, 38,
      21,  0,  0,  0, 17, 16, 16, 16, 16, 16, 16, 28,  0, 25, 20,
      21,  0,  0,  0, 17, 16, 16, 16, 16, 16, 20,  0,  0,  0, 21,
      21,  0,  0,  0, 17, 16, 16, 24, 16, 16, 16, 22,  0, 19, 20,
      17, 18, 18, 18, 16, 16, 20,  0, 17, 16, 16, 16, 18, 16, 20,
      17, 16, 16, 16, 16, 16, 20,  0, 17, 16, 16, 16, 16, 16, 20,
      17, 16, 16, 16, 24, 24, 28,  0, 25, 24, 24, 16, 16, 16, 20,
      17, 16, 16, 20,  0,  0,  0,  0,  0,  0,  0, 17, 16, 16, 20,
      17, 16, 16, 16, 18, 18, 22,  0, 19, 18, 18, 16, 16, 16, 20,
      17, 16, 16, 16, 16, 16, 20,  0, 17, 64, 16, 16, 16, 16, 20,
      17, 16, 24, 16, 16, 16, 20,  0, 17, 16, 16, 24, 24, 24, 20,
      17, 28,  0, 25, 16, 16, 16, 18, 16, 16, 20,  0,  0,  0, 21,
      21,  0,  0,  0, 17, 16, 16, 16, 16, 16, 20,  0,  0,  0, 21,
      17, 22,  0, 19, 16, 16, 16, 16, 16, 16, 20,  0,  0,  0, 21,
      41, 24, 26, 24, 24, 24, 24, 24, 24, 24, 24, 26, 26, 26, 44 },
     
     //level five
     
     {35, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 38,
      17, 16, 16, 16, 24, 16, 16, 16, 16, 16, 24, 16, 16, 16, 20,
      17, 16, 16, 20,  0, 17, 16, 16, 16, 20,  0, 17, 16, 16, 20,
      17, 16, 16, 20,  0, 17, 16, 16, 16, 20,  0, 17, 16, 16, 20,
      17, 16, 16, 20,  0, 17, 16, 16, 16, 20,  0, 17, 16, 16, 20,
      17, 16, 16, 20,  0, 17, 16, 16, 16, 20,  0, 17, 16, 16, 20,
      17, 16, 16, 20,  0, 17, 16, 16, 16, 20,  0, 17, 16, 16, 20,
      17, 16, 16, 16, 18, 16, 16, 16, 16, 16, 18, 16, 16, 16, 20,
      17, 16, 16, 24, 16, 16, 16, 64, 16, 16, 16, 24, 16, 16, 20,
      17, 16, 20,  0, 17, 16, 16, 16, 16, 16, 20,  0, 17, 16, 20,
      17, 16, 20,  0, 17, 16, 16, 16, 16, 16, 20,  0, 17, 16, 20,
      17, 16, 20,  0, 25, 24, 24, 24, 24, 24, 28,  0, 17, 16, 20,
      17, 16, 20,  0,  0,  0,  0,  0,  0,  0,  0,  0, 17, 16, 20,
      17, 16, 16, 18, 18, 18, 18, 18, 18, 18, 18, 18, 16, 16, 20,
      41, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 44 }
     
    	}; 

  final int validspeeds[] = { 1, 2, 3, 4, 6, 8 };
  final int maxspeed = 6;

  int currentspeed = 3;
  short[] screendata;
  Timer timer; 
  Date peldatetime;
  
  //pelltime is the original time the pellet was eaten
  //pelltimeup is the number that is incremented if ppellet is true
  long pelltime; 
  long pelltimeup; 
  
	
  public Board() {

    GetImages();

    addKeyListener(new TAdapter());

    screendata = new short[nrofblocks * nrofblocks];
    mazecolor = new Color(5, 100, 5);
    setFocusable(true);

    d = new Dimension(400, 400);

    setBackground(Color.black);
    setDoubleBuffered(true);

    state = State.NONE; 
	  
    ghostx = new int[maxghosts];
    ghostdx = new int[maxghosts];
    ghosty = new int[maxghosts];
    ghostdy = new int[maxghosts];
    ghostspeed = new int[maxghosts];
    dx = new int[4];
    dy = new int[4];
    timer = new Timer(40, this);
    timer.start();
  }

  public void addNotify() {
    super.addNotify();
    GameInit();
  }

  //counts pacman's position to show which image should be drawn (direction)
  public void DoAnim() {
    pacanimcount--;
    if (pacanimcount <= 0) {
      pacanimcount = pacanimdelay;
      pacmananimpos = pacmananimpos + pacanimdir;
      if (pacmananimpos == (pacmananimcount - 1) || pacmananimpos == 0)
        pacanimdir = -pacanimdir;
    }
  }


  public void PlayGame(Graphics2D g2d) {
    if (dying) {
      state = State.RESTART;
      Death();
      Sound sound = SoundFactory.getInstance(SOUND_DEATH);
      SoundFactory.play(sound);
    } 
    else {
      MovePacMan();
      DrawPacMan(g2d);
      moveGhosts(g2d);
      CheckMaze();
    }
  }

  //  prompts player to start the game
  public void ShowIntroScreen(Graphics2D g2d) {

    g2d.setColor(new Color(0, 32, 48));
    g2d.fillRect(50, scrsize / 2 - 30, scrsize - 100, 50);
    g2d.setColor(Color.white);
    g2d.drawRect(50, scrsize / 2 - 30, scrsize - 100, 50);

    String s = "Press s to start";
    Font small = new Font("Helvetica", Font.BOLD, 25);
    FontMetrics metr = this.getFontMetrics(small);

    g2d.setColor(Color.white);
    g2d.setFont(small);
    g2d.drawString(s, (scrsize - metr.stringWidth(s)) / 2, scrsize / 2);
  }
	// restart screen
  public void ShowRestartScreen(Graphics2D g2d) {

	    g2d.setColor(new Color(0, 32, 48));
	    g2d.fillRect(50, scrsize / 2 - 30, scrsize - 100, 50);
	    g2d.setColor(Color.white);
	    g2d.drawRect(50, scrsize / 2 - 30, scrsize - 100, 50);

	    String s = "Game Over. You lost. Press s to restart";
	    Font small = new Font("Helvetica", Font.BOLD, 25);
	    FontMetrics metr = this.getFontMetrics(small);

	    g2d.setColor(Color.white);
	    g2d.setFont(small);
	    g2d.drawString(s, (scrsize - metr.stringWidth(s)) / 2, scrsize / 2);
  }
  public void ShowNextScreen(Graphics2D g2d) {

	    g2d.setColor(new Color(0, 32, 48));
	    g2d.fillRect(50, scrsize / 2 - 30, scrsize - 100, 50);
	    g2d.setColor(Color.white);
	    g2d.drawRect(50, scrsize / 2 - 30, scrsize - 100, 50);

	    String s = "Nice Job! Press s to continue to the next level.";
	    Font small = new Font("Helvetica", Font.BOLD, 25);
	    FontMetrics metr = this.getFontMetrics(small);

	    g2d.setColor(Color.white);
	    g2d.setFont(small);
	    g2d.drawString(s, (scrsize - metr.stringWidth(s)) / 2, scrsize / 2);
  }
  public void ShowWinScreen(Graphics2D g2d) {

	    g2d.setColor(new Color(0, 32, 48));
	    g2d.fillRect(50, scrsize / 2 - 30, scrsize - 100, 50);
	    g2d.setColor(Color.white);
	    g2d.drawRect(50, scrsize / 2 - 30, scrsize - 100, 50);

	    String s = "You won!! Press s to restart.";
	    Font small = new Font("Helvetica", Font.BOLD, 25);
	    FontMetrics metr = this.getFontMetrics(small);

	    g2d.setColor(Color.white);
	    g2d.setFont(small);
	    g2d.drawString(s, (scrsize - metr.stringWidth(s)) / 2, scrsize / 2);
  }

  // displays the players score
  public void DrawScore(Graphics2D g) {
    int i;
    String s;
    String h;

    g.setFont(smallfont);
    g.setColor(new Color(96, 128, 255));
    s = "Score: " + score;
    h = "Highscore: " + highscore;
    g.drawString(s, scrsize / 6 + 192, scrsize + 30);
    for (i = 0; i < pacsleft; i++) {
      g.drawImage(pacman3left, i * 56 + 18, scrsize + 2, this);
    }
    g.drawString(h, scrsize / 6 + 325, scrsize + 30);
  }

  public void CheckScore(){
    
    if(highscore.equals("")) {
      return;
    }

    if(score > Integer.parseInt((highscore.split(":")[1]))) {
      //user setting a new highscore
      String name = JOptionPane.showInputDialog("New Highscore! Enter name!");
      highscore = " " + name + " : " + score;

      File scoreFile = new File("highscore.dat");
      if(!scoreFile.exists()) {
        try {
          scoreFile.createNewFile();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      FileWriter writeFile = null;
      BufferedWriter writer = null;
      try {
        writeFile = new FileWriter(scoreFile);
        writer = new BufferedWriter(writeFile);
        writer.write(this.highscore);
      }
      catch(Exception e) {
        //errors
      }
      finally {
        try {
          if(writer != null) {
            writer.close();
          }
        }
        catch(Exception e){
          //errors
        }
      }
    }
  }


  public void CheckMaze() {
    short i = 0;
    //  checks to see if there are any pellets left for pacman to eat
    boolean finished = true;

    while (i < nrofblocks * nrofblocks && finished) {
      if ((screendata[i] & 48) != 0)
        finished = false;
      i++;
    }
    // when all pellets are eaten, move to next level
    if (finished) {
	    
      //Checks if its on Level 5 and if it is state is win screen. if not state is next level
      if (level == leveldata.length - 1) {
            ingame = false;
	    state = State.DONE;
	    CheckScore();
      } 
      else if (level < leveldata.length - 1) {
	    state = State.NEXT; 
      }
      
      if (nrofghosts < maxghosts){
        nrofghosts++;
      }
      if (currentspeed < maxspeed){
        currentspeed++;
      }
      LevelInit();
    }
  }

  public void Death() {

    pacsleft--;
    if (pacsleft == 0) {
      ingame = false; 
      state = State.RESTART; 
      CheckScore();
      LevelInit(); 
      score = 0;
    }
    LevelContinue();
  }

  public void PowerPelletTimer() {
	  
	  //while ppellet is true, check the time, this refreshes every time
	  //the screen repaints to check if ppellet is true and ++pelltimeup
	  if (ppellet == true) {
		  
		  ++pelltimeup;
	   
		  if (pelltimeup == pelltime+250) {
			  ppellet = false; 
		  }
	  }
  }


  public void moveGhosts(Graphics2D g2d) {
    short i;
    int pos;
    int count;

    for (i = 0; i < nrofghosts; i++) {
      if (ghostx[i] % blocksize == 0 && ghosty[i] % blocksize == 0) {
        pos = ghostx[i] / blocksize + nrofblocks * (int)(ghosty[i] / blocksize);

        count = 0;
        if ((screendata[pos] & 1) == 0 && ghostdx[i] != 1) {
          dx[count] = -1;
          dy[count] = 0;
          count++;
        }
        if ((screendata[pos] & 2) == 0 && ghostdy[i] != 1) {
          dx[count] = 0;
          dy[count] = -1;
          count++;
        }
        if ((screendata[pos] & 4) == 0 && ghostdx[i] != -1) {
          dx[count] = 1;
          dy[count] = 0;
          count++;
        }
        if ((screendata[pos] & 8) == 0 && ghostdy[i] != -1) {
          dx[count] = 0;
          dy[count] = 1;
          count++;
        }

        if (count == 0) {
          if ((screendata[pos] & 15) == 15) {
            ghostdx[i] = 0;
            ghostdy[i] = 0;
          } else {
            ghostdx[i] = -ghostdx[i];
            ghostdy[i] = -ghostdy[i];
          }
        } else {
          count = (int)(Math.random() * count);
          if (count > 3)
            count = 3;
          ghostdx[i] = dx[count];
          ghostdy[i] = dy[count];
        }
      }
      ghostx[i] = ghostx[i] + (ghostdx[i] * ghostspeed[i]);
      ghosty[i] = ghosty[i] + (ghostdy[i] * ghostspeed[i]);
      DrawGhost(g2d, ghostx[i] + 1, ghosty[i] + 1);

      if (pacmanx > (ghostx[i] - 12) && pacmanx < (ghostx[i] + 12) &&
          pacmany > (ghosty[i] - 12) && pacmany < (ghosty[i] + 12) &&
          ingame) {

        dying = true;
        deathcounter = 64;

      }
      if (pacmanx > (ghostx[i] - 12) && pacmanx < (ghostx[i] + 12) &&
          pacmany > (ghosty[i] - 12) && pacmany < (ghosty[i] + 12) &&
          ingame && ppellet) {
        dying = false;
        ghostx[i] = 4 * blocksize;
        ghosty[i] = 4 * blocksize;
        Sound sound = SoundFactory.getInstance(SOUND_EATGHOST);
        SoundFactory.play(sound);
      }
    }
  }


  public void DrawGhost(Graphics2D g2d, int x, int y) {
    if (ppellet == false) {
	    
    	g2d.drawImage(ghost, x, y, this);
	    
    } else if (ppellet == true) { 
	    
    	g2d.drawImage(pellghost, x, y, this);
	    
    }
  }


  public void MovePacMan() {
    int pos;
    short ch;

    if (reqdx == -pacmandx && reqdy == -pacmandy) {
      pacmandx = reqdx;
      pacmandy = reqdy;
      viewdx = pacmandx;
      viewdy = pacmandy;
    }
    if (pacmanx % blocksize == 0 && pacmany % blocksize == 0) {
      pos = pacmanx / blocksize + nrofblocks * (int)(pacmany / blocksize);
      ch = screendata[pos];

      //changes the score when regular pellet is eaten
      if ((ch & 16) != 0) {
        screendata[pos] = (short)(ch & 15);
        score++;
        Sound sound = SoundFactory.getInstance(SOUND_CHOMP);
        SoundFactory.play(sound);
      }
	    
      // cherry eating and bonus points
      if ((ch & 64) != 0) {
          screendata[pos] = (short)(ch & 15);
          score = score + 100;
          Sound sound = SoundFactory.getInstance(SOUND_POWERUP);
          SoundFactory.play(sound);
        }

      //changes the score when power pellet is eaten
      if ((ch & 32) != 0) {
        screendata[pos] = (short)(ch & 15);
        this.score = score + 40;

        ppellet = true;
        
        // new date/time
  	peldatetime = new Date(); 
    	// power pellet start time 
	pelltime = peldatetime.getTime();
    	// power pellet start time for counter
  	pelltimeup = peldatetime.getTime();
        
        Sound sound = SoundFactory.getInstance(SOUND_POWERUP);
        SoundFactory.play(sound);
      }

      if (reqdx != 0 || reqdy != 0) {
        if (!((reqdx == -1 && reqdy == 0 && (ch & 1) != 0) ||
            (reqdx == 1 && reqdy == 0 && (ch & 4) != 0) ||
            (reqdx == 0 && reqdy == -1 && (ch & 2) != 0) ||
            (reqdx == 0 && reqdy == 1 && (ch & 8) != 0))) {
          pacmandx = reqdx;
          pacmandy = reqdy;
          viewdx = pacmandx;
          viewdy = pacmandy;
        }
      }

      // Check for standstill
      if ((pacmandx == -1 && pacmandy == 0 && (ch & 1) != 0) ||
          (pacmandx == 1 && pacmandy == 0 && (ch & 4) != 0) ||
          (pacmandx == 0 && pacmandy == -1 && (ch & 2) != 0) ||
          (pacmandx == 0 && pacmandy == 1 && (ch & 8) != 0)) {
        pacmandx = 0;
        pacmandy = 0;
      }
    }
    pacmanx = pacmanx + pacmanspeed * pacmandx;
    pacmany = pacmany + pacmanspeed * pacmandy;
  }

  public void DrawCherry(Graphics2D g2d) {
    g2d.drawImage(cherries, 40, 40, this);
  }

  public void DrawPacMan(Graphics2D g2d) {
    if (viewdx == -1)
      DrawPacManLeft(g2d);
    else if (viewdx == 1)
      DrawPacManRight(g2d);
    else if (viewdy == -1)
      DrawPacManUp(g2d);
    else
      DrawPacManDown(g2d);
  }

  public void DrawPacManUp(Graphics2D g2d) {
    switch (pacmananimpos) {
    case 1:
      g2d.drawImage(pacman2up, pacmanx + 1, pacmany + 1, this);
      break;
    case 2:
      g2d.drawImage(pacman3up, pacmanx + 1, pacmany + 1, this);
      break;
    case 3:
      g2d.drawImage(pacman4up, pacmanx + 1, pacmany + 1, this);
      break;
    default:
      g2d.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
      break;
    }
  }


  public void DrawPacManDown(Graphics2D g2d) {
    switch (pacmananimpos) {
    case 1:
      g2d.drawImage(pacman2down, pacmanx + 1, pacmany + 1, this);
      break;
    case 2:
      g2d.drawImage(pacman3down, pacmanx + 1, pacmany + 1, this);
      break;
    case 3:
      g2d.drawImage(pacman4down, pacmanx + 1, pacmany + 1, this);
      break;
    default:
      g2d.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
      break;
    }
  }


  public void DrawPacManLeft(Graphics2D g2d) {
    switch (pacmananimpos) {
    case 1:
      g2d.drawImage(pacman2left, pacmanx + 1, pacmany + 1, this);
      break;
    case 2:
      g2d.drawImage(pacman3left, pacmanx + 1, pacmany + 1, this);
      break;
    case 3:
      g2d.drawImage(pacman4left, pacmanx + 1, pacmany + 1, this);
      break;
    default:
      g2d.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
      break;
    }
  }


  public void DrawPacManRight(Graphics2D g2d) {
    switch (pacmananimpos) {
    case 1:
      g2d.drawImage(pacman2right, pacmanx + 1, pacmany + 1, this);
      break;
    case 2:
      g2d.drawImage(pacman3right, pacmanx + 1, pacmany + 1, this);
      break;
    case 3:
      g2d.drawImage(pacman4right, pacmanx + 1, pacmany + 1, this);
      break;
    default:
      g2d.drawImage(pacman1, pacmanx + 1, pacmany + 1, this);
      break;
    }
  }


  public void DrawMaze(Graphics2D g2d) {
    short i = 0;
    int x, y;

    for (y = 0; y < scrsize; y += blocksize) {
      for (x = 0; x < scrsize; x += blocksize) {
        g2d.setColor(mazecolor);
        g2d.setStroke(new BasicStroke(2));

        if ((screendata[i] & 1) != 0) // draws left
        {
          g2d.drawLine(x, y, x, y + blocksize - 1);
        }
        if ((screendata[i] & 2) != 0) // draws top
        {
          g2d.drawLine(x, y, x + blocksize - 1, y);
        }
        if ((screendata[i] & 4) != 0) // draws right
        {
          g2d.drawLine(x + blocksize - 1, y, x + blocksize - 1,
              y + blocksize - 1);
        }
        if ((screendata[i] & 8) != 0) // draws bottom
        {
          g2d.drawLine(x, y + blocksize - 1, x + blocksize - 1,
              y + blocksize - 1);
        }
        if ((screendata[i] & 16) != 0) // draws point
        {
          g2d.setColor(dotcolor);
          g2d.fillRect(x + 11, y + 11, 4, 4);
        }
        if ((screendata[i] & 32) != 0) // draws power pellet
        {
          g2d.setColor(pelletcolor);
          g2d.fillOval(x + 8, y + 8, 15, 15);
        }
        if ((screendata[i] & 64) != 0) { //draws cherry
          g2d.drawImage(cherries, x, y, this);
        }
        i++;
      }
    }
  }

  public void GameInit() {
    state = State.START; 
    level = 0; 
    pacsleft = 3;
    score = 0;
    LevelInit();
    nrofghosts = 6;
    currentspeed = 3;
     
  }


  public void LevelInit() {
    int i;
    //if state is start or restart, reset the level
    if (state == State.START || state == State.RESTART) {
    	for (i = 0; i < nrofblocks * nrofblocks; i++)
    	      screendata[i] = leveldata[level][i];
    }
    pacsleft = 3;
    //if state is next, move to the next level
    if (state == State.NEXT) {
    	if (level < leveldata.length - 1) {
            level += 1; 
        	
	    for(i = 0; i < nrofblocks * nrofblocks; i++) {
        	screendata[i] = leveldata[level][i];
            }
        }
    pacsleft = 3; 
    }
    //if state is done, set level 1 and redraw maze
    if (state == State.DONE) {
	level = 0; 
	for (i = 0; i < nrofblocks * nrofblocks; i++)
	     screendata[i] = leveldata[level][i];
     }

    LevelContinue();
  }


  public void LevelContinue() {
    short i;
    int dx = 1;
    int random;

    for (i = 0; i < nrofghosts; i++) {
      ghosty[i] = 4 * blocksize;
      ghostx[i] = 4 * blocksize;
      ghostdy[i] = 0;
      ghostdx[i] = dx;
      dx = -dx;
      random = (int)(Math.random() * (currentspeed + 1));
      if (random > currentspeed)
        random = currentspeed;
      ghostspeed[i] = validspeeds[random];
    }

    pacmanx = 7 * blocksize;
    pacmany = 11 * blocksize;
    pacmandx = 0;
    pacmandy = 0;
    reqdx = 0;
    reqdy = 0;
    viewdx = -1;
    viewdy = 0;
    dying = false;
    
  }

	//all the images for the game
  public void GetImages(){

    ghost = new ImageIcon(Board.class.getResource("/res/ghost.png")).getImage();
    pellghost = new ImageIcon(Board.class.getResource("/res/pellghost.png")).getImage();
    pacman1 = new ImageIcon(Board.class.getResource("/res/pacman_test_44.png")).getImage();
    pacman2up = new ImageIcon(Board.class.getResource("/res/up1_44.png")).getImage();
    pacman3up = new ImageIcon(Board.class.getResource("/res/up2_44.png")).getImage();
    pacman4up = new ImageIcon(Board.class.getResource("/res/up3_44.png")).getImage();
    pacman2down = new ImageIcon(Board.class.getResource("/res/down1_44.png")).getImage();
    pacman3down = new ImageIcon(Board.class.getResource("/res/down2_44.png")).getImage(); 
    pacman4down = new ImageIcon(Board.class.getResource("/res/down3_44.png")).getImage();
    pacman2left = new ImageIcon(Board.class.getResource("/res/left1_44.png")).getImage();
    pacman3left = new ImageIcon(Board.class.getResource("/res/left2_44.png")).getImage();
    pacman4left = new ImageIcon(Board.class.getResource("/res/left3_44.png")).getImage();
    pacman2right = new ImageIcon(Board.class.getResource("/res/right1_44.png")).getImage();
    pacman3right = new ImageIcon(Board.class.getResource("/res/right2_44.png")).getImage();
    pacman4right = new ImageIcon(Board.class.getResource("/res/right3_44.png")).getImage();
    cherries = new ImageIcon(Board.class.getResource("/res/cherries.png")).getImage();
  }

  public void paint(Graphics g)
  { 
    super.paint(g);

    Graphics2D g2d = (Graphics2D) g;

    g2d.setColor(Color.black);
    g2d.fillRect(0, 0, d.width, d.height);

    DrawMaze(g2d);
    DrawScore(g2d);
    DoAnim();

    //next level memo
    if (ingame) {
      if (state == State.NEXT) {
	ShowNextScreen(g2d);
      }
    	
      PlayGame(g2d);
      
    } else if (state == State.START) {
      	ShowIntroScreen(g2d);
	    
    } else if (state == State.RESTART) {
        ShowRestartScreen(g2d); 
	    
    } else if (state == State.DONE) {
        ShowWinScreen(g2d);
    }
    if(highscore.equals("")) {
      highscore = this.GetHighScore();

    }
	
	  
    g.drawImage(ii, 5, 5, this);
    Toolkit.getDefaultToolkit().sync();
    g.dispose();
  }

  class TAdapter extends KeyAdapter {
    public void keyPressed(KeyEvent e) {

      int key = e.getKeyCode();

      if (ingame)
      {
        if (key == KeyEvent.VK_LEFT)
        {
          reqdx=-1;
          reqdy=0;
        }
        else if (key == KeyEvent.VK_RIGHT)
        {
          reqdx=1;
          reqdy=0;
        }
        else if (key == KeyEvent.VK_UP)
        {
          reqdx=0;
          reqdy=-1;
        }
        else if (key == KeyEvent.VK_DOWN)
        {
          reqdx=0;
          reqdy=1;
        }
        else if (key == KeyEvent.VK_ESCAPE && timer.isRunning())
        {
          ingame=false;
        }
        else if (key == KeyEvent.VK_PAUSE) {
          if (timer.isRunning())
            timer.stop();
          else timer.start();
	}
	if (key == 's' || key == 'S')
        {
          state = State.NONE;
          LevelInit();
        }
      }
      else
      {
        if (key == 's' || key == 'S')
        {
          ingame=true;
          LevelInit();
        }
      }
    }

    public void keyReleased(KeyEvent e) {
      int key = e.getKeyCode();

      if (key == Event.LEFT || key == Event.RIGHT || 
          key == Event.UP ||  key == Event.DOWN)
      {
        reqdx=0;
        reqdy=0;
      }
    }
  }

  public void actionPerformed(ActionEvent e) {
    repaint();
    PowerPelletTimer();
  }

  public String GetHighScore() {
    // format: AMS 100
    FileReader readFile = null;
    BufferedReader reader = null;

    try {
      readFile = new FileReader("highscore.dat");
      reader = new BufferedReader(readFile);
      return reader.readLine();
    } 
    catch (Exception e) {
      return "Nobody:0";
    }
    finally {
      try {
        if(reader != null)
          reader.close();
      }
      catch(IOException e) {
        e.printStackTrace();
      }
    }
  }
}
