package viewer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.io.*;

import model.World;
import model.Cell;

public class Viewer extends JFrame {

	private Board board;
	private World world;
	
	long slowDown = 2498;
	private boolean played = false;
	private boolean redLoaded, blackLoaded, worldLoaded = false;
	
	private int redFood;
	private JLabel redScore = new JLabel("Red score: 0");
	
	private int blackFood;
	private JLabel blackScore = new JLabel("Black score: 0");

	private int roundNo;
	private JLabel roundNumber = new JLabel("Round number: 0");

	private Button playButton,pauseButton,slowButton,fastButton,loadRedButton,loadBlackButton,loadWorldButton;
	
	private Panel panel = new Panel(new BorderLayout());
	private Panel panel1 = new Panel();
	private Panel panel2 = new Panel();
	private JFileChooser fc = new JFileChooser();

	private Button addButton(String name, Panel panel, ActionListener action) {
			Button b = new Button(name);
			b.addActionListener(action);
			panel.add(b);
			return b;
   }
	
    public Viewer() {
		super("Ant tournament viewer");
		world = World.v();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		getContentPane().setLayout(new BorderLayout());
	 
		board = new Board();
		getContentPane().add(board, BorderLayout.CENTER);
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.add(panel1, BorderLayout.WEST);
		panel.add(panel2, BorderLayout.EAST);
		
		panel1.add(redScore);
		panel1.add(blackScore);
		panel1.add(roundNumber);
		
		playButton = addButton("Play",panel2,new ActionListener () {
		      						public void actionPerformed(ActionEvent e) { if (paused) resume(); else play(); }
	     							});
		pauseButton = addButton("Pause",panel2,new ActionListener () {
								public void actionPerformed(ActionEvent e) { pause(); }
								});
		slowButton = addButton("Slower",panel2,new ActionListener() {
									public void actionPerformed(ActionEvent e) { slowDown = (slowDown + 1)*2; }
									});
		fastButton = addButton("Faster",panel2,new ActionListener() {
										public void actionPerformed(ActionEvent e) { if (slowDown > 1) slowDown = slowDown/2 - 1; }
									});
		loadRedButton = addButton("Load Red",panel2,antLoader(model.Color.RED));
		loadBlackButton = addButton("Load Black",panel2,antLoader(model.Color.BLACK));
		loadWorldButton = addButton("Load World",panel2,worldLoader());
													  
		playButton.setEnabled(false);
		pauseButton.setEnabled(false);
		slowButton.setEnabled(false);
		fastButton.setEnabled(false);
		addButton("Quit",panel2,new ActionListener() {
	    							public void actionPerformed(ActionEvent e) {System.exit(0); } });	
	   	      
		
		
		pack();
		setVisible(true);
	    
		Update.aspectOf().register(this);    
	 }
	 

	private class PlayThread extends Thread {
		public void run() {
			world.play();
			playButton.setEnabled(true);
			pauseButton.setEnabled(false);
			slowButton.setEnabled(false);
			fastButton.setEnabled(false);
			loadBlackButton.setEnabled(true);
			loadRedButton.setEnabled(true);
			loadWorldButton.setEnabled(true);
			roundNumber.setText("GAME OVER");
			roundNumber.invalidate();
			validate();
		}
	}
	
	private void enablePlay() {
		if (worldLoaded && blackLoaded && redLoaded)
			playButton.setEnabled(true);
	}
	
	private PlayThread player = null;
	
	private void play() {
		redFood =0; blackFood=0; slowDown = 2498;
		playButton.setEnabled(false);
		pauseButton.setEnabled(true);
		slowButton.setEnabled(true);
		fastButton.setEnabled(true);
		loadBlackButton.setEnabled(false);
		loadRedButton.setEnabled(false);
		loadWorldButton.setEnabled(false);
		if (played) {
			try {world.reset();
			} 	catch (IOException x) {
				JOptionPane.showMessageDialog(Viewer.this, "Cannot open world map: it must have changed since last run.");
			}
			board.reset();
			redScore.setText("Red score: 0");
			redScore.invalidate();
			blackScore.setText("Black score: 0");
			blackScore.invalidate();
			roundNumber.setText("Round number: 0");
			roundNumber.invalidate();
			validate();
			paint();
		}
		played =true;
	    player = new PlayThread();
		player.start();
	}

	private boolean paused = false; 
	
	private void pause () {
		assert(!paused);
		player.suspend();
		paused = true;
		playButton.setEnabled(true);
		pauseButton.setEnabled(false);
	}

	private void resume () {
		assert(paused);
		player.resume();
		paused = false;
		playButton.setEnabled(false);
		pauseButton.setEnabled(true);
	}
	
	private ActionListener antLoader(final int color) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = fc.showOpenDialog(Viewer.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					loadAnt(color, file);
				}
			}
		};
	}
	
	private ActionListener worldLoader() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int returnVal = fc.showOpenDialog(Viewer.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						loadWorld(file);
					}
				}
			};
		}
	
	public void paint() {
			paint(board.getGraphics());
	}
	
	public void update(Cell c) {
		board.getHexagon(c.getX(),c.getY()).render(board.getGraphics());
	}
	
	public void upRed(int newFood) {
		redFood += newFood;
		redScore.setText("Red score: "+redFood);
		redScore.invalidate();
		validate();
	}
	
	public void upBlack(int newFood) {
		blackFood += newFood;
		blackScore.setText("Black score: "+blackFood);
		blackScore.invalidate();
		validate();
	}
	
	public void incRound() {
		roundNo += 1;
		roundNumber.setText("Round number: "+roundNo);
		roundNumber.invalidate();
		validate();
	}

	public void loadAnt(int color, File file) {
		//System.err.printLn("loadAnt " + file.toString());
		try {
			world.loadAnt(color,file);
			board.setDirty();
			blackLoaded = blackLoaded || (color == model.Color.BLACK);
			redLoaded = redLoaded || (color == model.Color.RED);
			enablePlay();
		} catch (IOException x) {
			JOptionPane.showMessageDialog(Viewer.this, "Cannot open ant file "+file.getName());
		} catch (RuntimeException r) {
			JOptionPane.showMessageDialog(Viewer.this, "In " + file.getName() + ": "+r.getMessage());
		}
	}

	public void loadWorld (File file) {
		//System.err.printLn("loadWorld " + file.toString());
		try {
			world.loadWorld(file);
			board.reset();
			worldLoaded = true;
			enablePlay();
		} catch (IOException x) {
			JOptionPane.showMessageDialog(Viewer.this, "Cannot open world map "+file.getName());
		} catch (RuntimeException r) {
			JOptionPane.showMessageDialog(Viewer.this, "In " + file.getName() + ": "+r.getMessage());
							                                        }
	}
	
	public synchronized void paint(Graphics g) {
		 board.render(g);
	}
	
}
