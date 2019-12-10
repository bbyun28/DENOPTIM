package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import denoptim.exception.DENOPTIMException;
import denoptim.io.DenoptimIO;
import denoptim.molecule.DENOPTIMAttachmentPoint;
import denoptim.molecule.DENOPTIMEdge;
import denoptim.molecule.DENOPTIMGraph;
import denoptim.molecule.DENOPTIMRing;
import denoptim.molecule.DENOPTIMVertex;


/**
 * A panel that understands DENOPTIM graphs and allows to create and edit
 * them.
 * 
 * @author Marco Foscato
 */

public class GUIGraphHandler extends GUICardPanel
{
	/**
	 * Version UID
	 */
	private static final long serialVersionUID = -8303012362366503382L;
	
	/**
	 * Unique identified for instances of this handler
	 */
	public static AtomicInteger graphHandlerTabUID = new AtomicInteger(1);
	
	/**
	 * The currently loaded list of graphs
	 */
	private ArrayList<DENOPTIMGraph> dnGraphLibrary =
			new ArrayList<DENOPTIMGraph>();
	
	/**
	 * The currently loaded graph as DENOPTIM object
	 */
	private DENOPTIMGraph dnGraph;
	
	/**
	 * The currently loaded graph as GRaphStream object
	 */
	private Graph graph;
	
	/**
	 * The index of the currently loaded dnGraph [0–(n-1)}
	 */
	private int currGrphIdx = 0;
	
	/**
	 * Flag signaling that loaded data has changes since last save
	 */
	private boolean unsavedChanges = false;
	
	/**
	 * Flag signaling that there is a fully defined fragment space
	 */
	private boolean hasFragSpace = false;
	
	private GraphViewerPanel graphPanel;
	private JPanel graphCtrlPane;
	private JPanel graphNavigPane;
	
	private JButton btnAddGraph;
	private JButton btnGraphDel;
	
	private JButton btnOpenGraphs;
	
	private JSpinner graphNavigSpinner;
	private JLabel totalGraphsLabel;
	private final GraphSpinnerChangeEvent graphSpinnerListener = 
												new GraphSpinnerChangeEvent();
	
	private JPanel pnlEditVrtxBtns;
	private JButton btnAddVrtx;
	private JButton btnDelSel;
	
	private JPanel pnlShowLabels;
	private JButton btnAddLabel;
	private JButton btnDelLabel;
	private JComboBox<String> cmbLabel;
	
	private JPanel pnlSaveEdits;
	private JButton btnSaveEdits;
	
//-----------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public GUIGraphHandler(GUIMainPanel mainPanel)
	{
		super(mainPanel, "Graph Handler #" 
					+ graphHandlerTabUID.getAndIncrement());
		super.setLayout(new BorderLayout());
		initialize();
	}
	
//-----------------------------------------------------------------------------

	/**
	 * Initialize the panel and add buttons.
	 */
	private void initialize() 
	{	
		// BorderLayout is needed to allow dynamic resizing!
		this.setLayout(new BorderLayout()); 
		
		// This card structure includes center, east and south panels:
		// - (Center) graph viewer
		// - (East) graph controls
		// - (South) general controls (load, save, close)
		
		// The graph viewer goes all in here	
		graphPanel = new GraphViewerPanel();
		this.add(graphPanel,BorderLayout.CENTER);
       
		// General panel on the right: it containing all controls
        graphCtrlPane = new JPanel();
        graphCtrlPane.setVisible(true);
        graphCtrlPane.setLayout(new BoxLayout(graphCtrlPane, SwingConstants.VERTICAL));

        // Controls to navigate the list of dnGraphs
        graphNavigPane = new JPanel();
        JLabel navigationLabel1 = new JLabel("Graph # ");
        JLabel navigationLabel2 = new JLabel("Current library size: ");
        totalGraphsLabel = new JLabel("0");
        
		graphNavigSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 0, 1));
		graphNavigSpinner.setToolTipText("Move to graph number # in the currently loaded library.");
		graphNavigSpinner.setMaximumSize(new Dimension(75,20));
		graphNavigSpinner.addChangeListener(graphSpinnerListener);
        
		btnAddGraph = new JButton("Add");
		btnAddGraph.setToolTipText("Append a graph to the currently loaded "
				+ "list of graphs.");
		btnAddGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] options = new String[]{"Build", "File", "Cancel"};
				int res = JOptionPane.showOptionDialog(null,
		                "<html>Please choose how wherther to start creations "
		                + "of a new graph, or import graph from file.</html>",
		                "Specify source of new graph",
		                JOptionPane.DEFAULT_OPTION,
		                JOptionPane.QUESTION_MESSAGE,
		                UIManager.getIcon("OptionPane.warningIcon"),
		                options,
		                options[2]);
				switch (res)
				{
					case 0:
						
						break;
					
					case 1:
						File inFile = DenoptimGUIFileOpener.pickFile();
						if (inFile == null || inFile.getAbsolutePath().equals(""))
						{
							return;
						}
						appendGraphsFromFile(inFile);
						break;
				}
			}
		});
		btnGraphDel = new JButton("Remove");
		btnGraphDel.setToolTipText("Remove the present graph from the library.");
		btnGraphDel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try 
				{
					removeCurrentdnGraph();
				} catch (DENOPTIMException e1) {
					System.out.println("Esception while removing the current graph:");
					e1.printStackTrace();
				}
			}
		});
		
        GroupLayout lyoAddDetGraphs = new GroupLayout(graphNavigPane);
        graphNavigPane.setLayout(lyoAddDetGraphs);
        lyoAddDetGraphs.setAutoCreateGaps(true);
        lyoAddDetGraphs.setAutoCreateContainerGaps(true);
        lyoAddDetGraphs.setHorizontalGroup(lyoAddDetGraphs.createParallelGroup(
                        		GroupLayout.Alignment.CENTER)
                        .addGroup(lyoAddDetGraphs.createSequentialGroup()
                                        .addComponent(navigationLabel1)
                                        .addComponent(graphNavigSpinner))
                        .addGroup(lyoAddDetGraphs.createSequentialGroup()
                                        .addComponent(navigationLabel2)
                                        .addComponent(totalGraphsLabel))
                        .addGroup(lyoAddDetGraphs.createSequentialGroup()
                                        .addComponent(btnAddGraph)
                                        .addComponent(btnGraphDel)));
        lyoAddDetGraphs.setVerticalGroup(lyoAddDetGraphs.createSequentialGroup()
                        .addGroup(lyoAddDetGraphs.createParallelGroup(
                        		GroupLayout.Alignment.CENTER)
                                        .addComponent(navigationLabel1)
                                        .addComponent(graphNavigSpinner))
                        .addGroup(lyoAddDetGraphs.createParallelGroup()
                                        .addComponent(navigationLabel2)
                                        .addComponent(totalGraphsLabel))
                        .addGroup(lyoAddDetGraphs.createParallelGroup()
                                        .addComponent(btnAddGraph)
                                        .addComponent(btnGraphDel)));
		graphCtrlPane.add(graphNavigPane);
		
		graphCtrlPane.add(new JSeparator());
		
		// Controls to alter the presently loaded graph (if any)
		pnlEditVrtxBtns = new JPanel();
		JLabel edtVertxsLab = new JLabel("Edit verteces:");
		btnAddVrtx = new JButton("Add");
		btnAddVrtx.setToolTipText("<html>Append a vertex to the selected "
				+ "attachment point<html>");
		btnAddVrtx.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO: disable when no graph loaded
				//TODO: get selected
				ArrayList<DENOPTIMAttachmentPoint> selectedAps = 
						new ArrayList<DENOPTIMAttachmentPoint>();
				if (selectedAps.size() == 0)
				{
					JOptionPane.showMessageDialog(null,
			                "<html>No attachment point selected!<br> Drag the "
			                + "mouse to select APs.<br> "
			                + "Click again to unselect.</html>",
			                "Error",
			                JOptionPane.ERROR_MESSAGE,
			                UIManager.getIcon("OptionPane.errorIcon"));
					return;
				}
				else
				{
					//TODO 
					
			        // Protect the temporary "dnGraph" obj
			        unsavedChanges = true;
			        protectEditedSystem();
				}
			}
		});
		
		btnDelSel = new JButton("Remove");
		btnDelSel.setToolTipText("<html>Removes all selected vertexes from the "
				+ "system.<br><br><b>WARNING:</b> this action cannot be "
				+ "undone!");
		btnDelSel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO disable when no graph loaded
				//TODO: get selected
				ArrayList<DENOPTIMVertex> selectedVrtx = 
						new ArrayList<DENOPTIMVertex>();
				
				if (selectedVrtx.size() == 0)
				{
					JOptionPane.showMessageDialog(null,
							"<html>No vertex selected! Drag the "
			                + "mouse to select vertexes."
					        + "<br>Click on background to unselect.</html>",
			                "Error",
			                JOptionPane.ERROR_MESSAGE,
			                UIManager.getIcon("OptionPane.errorIcon"));
					return;
				}
				else
				{
					//TODO
					
			        // Protect the temporary "dnGraph" obj
			        unsavedChanges = true;
			        protectEditedSystem();
				}
			}
		});
		
		GroupLayout lyoEditVertxs = new GroupLayout(pnlEditVrtxBtns);
		pnlEditVrtxBtns.setLayout(lyoEditVertxs);
		lyoEditVertxs.setAutoCreateGaps(true);
		lyoEditVertxs.setAutoCreateContainerGaps(true);
		lyoEditVertxs.setHorizontalGroup(lyoEditVertxs.createParallelGroup(
				GroupLayout.Alignment.CENTER)
				.addComponent(edtVertxsLab)
				.addGroup(lyoEditVertxs.createSequentialGroup()
						.addComponent(btnAddVrtx)
						.addComponent(btnDelSel)));
		lyoEditVertxs.setVerticalGroup(lyoEditVertxs.createSequentialGroup()
				.addComponent(edtVertxsLab)
				.addGroup(lyoEditVertxs.createParallelGroup()
						.addComponent(btnAddVrtx)
						.addComponent(btnDelSel)));
		graphCtrlPane.add(pnlEditVrtxBtns);
		
		graphCtrlPane.add(new JSeparator());
		
		// Controls of displayed attributes
		pnlShowLabels = new JPanel();
		JLabel lblShowHideLabels = new JLabel("Manage graph labels:");
		cmbLabel = new JComboBox<String>(new String[] {graphPanel.SPRITE_APCLASS, 
				graphPanel.SPRITE_BNDORD, graphPanel.SPRITE_FRGID});
		cmbLabel.setToolTipText("<html>Select the kind of type of information"
				+ "<br>to add or remove from the graph view.</html>");
		//TODO: enable only if a graph is shown
		btnAddLabel = new JButton("Show");
		btnAddLabel.setToolTipText("<html>Shows the chosen label for the "
				+ "<br>selected elements.</html>");
		btnAddLabel.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				if (graphPanel.hasSelected())
				{
					graphPanel.appendSprites(cmbLabel.getSelectedItem().toString());
				}
				else
				{
					JOptionPane.showMessageDialog(null,
							"<html>No elements selected! Drag the "
			                + "mouse to select elements."
					        + "<br>Click on background to unselect.</html>",
			                "Error",
			                JOptionPane.ERROR_MESSAGE,
			                UIManager.getIcon("OptionPane.errorIcon"));
				}
			}
		});
		btnDelLabel = new JButton("Hide");
		btnDelLabel.setToolTipText("<html>Hides the chosen label for the "
				+ "<br>selected elements.</html>");
		btnDelLabel.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				if (graphPanel.hasSelected())
				{
					graphPanel.removeSprites(cmbLabel.getSelectedItem().toString());
				}
				else
				{
					JOptionPane.showMessageDialog(null,
							"<html>No elements selected! Drag the "
			                + "mouse to select elements."
					        + "<br>Click on background to unselect.</html>",
			                "Error",
			                JOptionPane.ERROR_MESSAGE,
			                UIManager.getIcon("OptionPane.errorIcon"));
				}
			}
		});
		
        GroupLayout lyoShowAttr = new GroupLayout(pnlShowLabels);
        pnlShowLabels.setLayout(lyoShowAttr);
        lyoShowAttr.setAutoCreateGaps(true);
        lyoShowAttr.setAutoCreateContainerGaps(true);
        lyoShowAttr.setHorizontalGroup(lyoShowAttr.createParallelGroup(
                        GroupLayout.Alignment.CENTER)
                        .addComponent(lblShowHideLabels)
                        .addComponent(cmbLabel)
                        .addGroup(lyoShowAttr.createSequentialGroup()
	                        .addComponent(btnAddLabel)
	                        .addComponent(btnDelLabel)));
        lyoShowAttr.setVerticalGroup(lyoShowAttr.createSequentialGroup()
		                .addComponent(lblShowHideLabels)
		                .addComponent(cmbLabel)
		                .addGroup(lyoShowAttr.createParallelGroup()
		                        .addComponent(btnAddLabel)
		                        .addComponent(btnDelLabel)));
        graphCtrlPane.add(pnlShowLabels);
        
        graphCtrlPane.add(new JSeparator());
		
		// Control for unsaved changes
        pnlSaveEdits = new JPanel();
        btnSaveEdits = new JButton("Save Changes");
        btnSaveEdits.setForeground(Color.RED);
        btnSaveEdits.setEnabled(false);
        btnSaveEdits.setToolTipText("<html>Save the current graph replacing"
        	+ " <br>the original one in the currently loaded library.</html>");
        btnSaveEdits.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	saveUnsavedChanges();
                }
        });
        pnlSaveEdits.add(btnSaveEdits);
        graphCtrlPane.add(pnlSaveEdits);
		
		this.add(graphCtrlPane,BorderLayout.EAST);
		
		
		// Panel with buttons to the bottom of the frame
		
		JPanel commandsPane = new JPanel();
		super.add(commandsPane, BorderLayout.SOUTH);
		
		btnOpenGraphs = new JButton("Load Library of graphs",
					UIManager.getIcon("FileView.directoryIcon"));
		btnOpenGraphs.setToolTipText("Reads graphs or structures from file.");
		btnOpenGraphs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File inFile = DenoptimGUIFileOpener.pickFile();
				if (inFile == null || inFile.getAbsolutePath().equals(""))
				{
					return;
				}
				importGraphsFromFile(inFile);
			}
		});
		commandsPane.add(btnOpenGraphs);
		
		JButton btnSaveFrags = new JButton("Save Library of graphs",
				UIManager.getIcon("FileView.hardDriveIcon"));
		btnSaveFrags.setToolTipText("Write all graphs to a file.");
		btnSaveFrags.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File outFile = DenoptimGUIFileOpener.saveFile();
				if (outFile == null)
				{
					return;
				}
				try
				{
					///TODO
				}
				catch (Exception ex)
				{
					JOptionPane.showMessageDialog(null,
			                "Could not write to '" + outFile + "'!.",
			                "Error",
			                JOptionPane.PLAIN_MESSAGE,
			                UIManager.getIcon("OptionPane.errorIcon"));
					return;
				}
				deprotectEditedSystem();
				unsavedChanges = false;
			}
		});
		commandsPane.add(btnSaveFrags);

		JButton btnCanc = new JButton("Close Tab");
		btnCanc.setToolTipText("Closes this graph handler.");
		btnCanc.addActionListener(new removeCardActionListener(this));
		commandsPane.add(btnCanc);
		
		JButton btnHelp = new JButton("?");
		btnHelp.setToolTipText("<html>Hover over the buttons and fields "
                    + "to get a tip.</html>");
		btnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
                    "<html>Hover over the buttons and fields "
                    + "to get a tip.</html>",
                    "Tips",
                    JOptionPane.PLAIN_MESSAGE);
			}
		});
		commandsPane.add(btnHelp);
		
		//TODO del
		//importGraphsFromFile(new File("/Users/mfo051/___/__GRAPH.sdf"));
	}
	
//-----------------------------------------------------------------------------

	/**
	 * Imports graphs from file. 
	 * @param file the file to open
	 */

	public void importGraphsFromFile(File file)
	{	
		dnGraphLibrary = readGraphsFromFile(file);	
			
		// Display the first
		currGrphIdx = 0;
		
		loadCurrentGraphIdxToViewer();
		updateGraphListSpinner();
	}

//-----------------------------------------------------------------------------

	private void appendGraphsFromFile(File file)
	{
		ArrayList<DENOPTIMGraph> graphs = readGraphsFromFile(file);
		int oldSize = dnGraphLibrary.size();
		if (graphs.size() > 0)
		{
			dnGraphLibrary.addAll(graphs);
			
			// Display the first of the imported ones
			currGrphIdx = oldSize;
			
			loadCurrentGraphIdxToViewer();
			updateGraphListSpinner();
		}
	}
	
//-----------------------------------------------------------------------------

	private ArrayList<DENOPTIMGraph> readGraphsFromFile(File file)
	{
		ArrayList<DENOPTIMGraph> graphs = new ArrayList<DENOPTIMGraph>();
		
		String format="SDF";
		
		//TODO: detect other formats or selection of format from GUI
		
		try 
		{
			graphs = DenoptimIO.readDENOPTIMGraphsFromFile(
					file.getAbsolutePath(), format, hasFragSpace);	
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			String msg = "<html>Could not read file '" + file.getAbsolutePath() 
	                + "'!<br>Hint of cause: ";
			if (e.getCause() != null)
			{
				msg = msg + e.getCause();
			}
			if (e.getMessage() != null)
			{
				msg = msg + " " + e.getMessage();
			}
			msg = msg + "</html>";
			JOptionPane.showMessageDialog(null,msg,
	                "Error",
	                JOptionPane.PLAIN_MESSAGE,
	                UIManager.getIcon("OptionPane.errorIcon"));
		}
		return graphs;
	}
	
//-----------------------------------------------------------------------------
	
	/**
	 * Loads the graph corresponding to the field {@link #currGrphIdx}
	 */
	private void loadCurrentGraphIdxToViewer()
	{
		if (dnGraphLibrary == null)
		{
			JOptionPane.showMessageDialog(null,
	                "No list of graphs loaded.",
	                "Error",
	                JOptionPane.PLAIN_MESSAGE,
	                UIManager.getIcon("OptionPane.errorIcon"));
			return;
		}
		
		clearCurrentSystem();
		graph = convertDnGraphToGSGraph(dnGraphLibrary.get(currGrphIdx));
		graphPanel.loadGraphToViewer(graph);
	}
	
//-----------------------------------------------------------------------------
	
	/**
	 * Created a graph object suitable for GraphStrem viewer from a 
	 * DENOPTIMGraph.
	 * @param dnG the graph to be converted
	 * @return the GraphStream object
	 */
	private Graph convertDnGraphToGSGraph(DENOPTIMGraph dnG) 
	{
		//TODO: test no edges/nodes
		
		graph = new SingleGraph("DENOPTIMGraph#"+dnG.getGraphId());
		
		for (DENOPTIMVertex v : dnG.getVertexList())
		{
			// Create representation of this vertex
			
			String vID = Integer.toString(v.getVertexId());
			
			Node n = graph.addNode(vID);
			n.addAttribute("ui.label", vID);
			n.setAttribute("dnp.molID", v.getMolId());
			n.setAttribute("dnp.frgType", v.getFragmentType());
			switch (v.getFragmentType())
			{
				case 0:
					n.setAttribute("ui.class", "scaffold");
					break;
				case 1:
					n.setAttribute("ui.class", "fragment");
					break;
				case 2:
					n.setAttribute("ui.class", "cap");
					break;
			}
			if (v.isRCV())
			{
				n.setAttribute("ui.class", "rcv");
			}
			n.setAttribute("dnp.level", v.getLevel());
			n.setAttribute("dnp.", "");
			
			// Create representation of free APs
			
			for (int i=0; i<v.getNumberOfAP(); i++)
			{
				DENOPTIMAttachmentPoint ap = v.getAttachmentPoints().get(i);
				if (ap.isAvailable())
				{
					String nApId = Integer.toString(i);
					Node nAP = graph.addNode(nApId);
					nAP.addAttribute("ui.label", nApId);
					nAP.setAttribute("ui.class", "ap");
					Edge eAP = graph.addEdge(vID+"-"+nApId,vID,nApId);
					eAP.setAttribute("ui.class", "ap");
					eAP.setAttribute("dnp.srcAPClass", ap.getAPClass());
				}
			}
		} 
		
		for (DENOPTIMEdge dnE : dnG.getEdgeList())
		{
			String srcIdx = Integer.toString(dnE.getSourceVertex());
			String trgIdx = Integer.toString(dnE.getTargetVertex());
			Edge e = graph.addEdge(srcIdx+"-"+trgIdx, srcIdx, trgIdx,true);
			e.setAttribute("dnp.srcAPId", dnE.getSourceDAP());
			e.setAttribute("dnp.trgAPId", dnE.getTargetDAP());
			e.setAttribute("dnp.srcAPClass", dnE.getSourceReaction());
			e.setAttribute("dnp.trgAPClass", dnE.getTargetReaction());
			e.setAttribute("dnp.bondType", dnE.getBondType());
		}
		 
		for (DENOPTIMRing r : dnG.getRings())
		{
			String srcIdx = Integer.toString(r.getHeadVertex().getVertexId());
			String trgIdx = Integer.toString(r.getTailVertex().getVertexId());
			Edge e = graph.addEdge(srcIdx+"-"+trgIdx, srcIdx, trgIdx,false);
			e.setAttribute("ui.class", "rc");
			
			//WARNING: graphs loaded without having a consistent definition of 
			// the fragment space will not have all the AP data (which should be 
			// taken from the fragment space). Therefore, they cannot be 
			// recognized as RCV, but here we can fix at least part of the issue
			
			graph.getNode(srcIdx).setAttribute("ui.class", "rcv");
			graph.getNode(trgIdx).setAttribute("ui.class", "rcv");
		}
		
		return graph;
	}
	
//-----------------------------------------------------------------------------
	
	private void clearCurrentSystem()
	{
		// Get rid of currently loaded graph
		dnGraph = null;
        graphPanel.cleanup();
	}

//-----------------------------------------------------------------------------

	private void updateGraphListSpinner()
	{		
		graphNavigSpinner.setModel(new SpinnerNumberModel(currGrphIdx+1, 1, 
				dnGraphLibrary.size(), 1));
		totalGraphsLabel.setText(Integer.toString(dnGraphLibrary.size()));
	}

//-----------------------------------------------------------------------------

	private class GraphSpinnerChangeEvent implements ChangeListener
	{
		private boolean inEnabled = true;
		
		public GraphSpinnerChangeEvent()
		{}
		
		/**
		 * Enables/disable the listener
		 * @param var <code>true</code> to activate listener, 
		 * <code>false</code> to disable.
		 */
		public void setEnabled(boolean var)
		{
			this.inEnabled = var;
		}
		
        @Override
        public void stateChanged(ChangeEvent event)
        {
        	if (!inEnabled)
        	{
        		return;
        	}
        	
        	//NB here we convert from 1-based index in GUI to 0-based index
        	currGrphIdx = ((Integer) graphNavigSpinner.getValue()).intValue() - 1;
        	loadCurrentGraphIdxToViewer();
        }
	}
	
//-----------------------------------------------------------------------------

	private void deprotectEditedSystem()
	{
		btnSaveEdits.setEnabled(false);
		btnAddGraph.setEnabled(true);
		btnOpenGraphs.setEnabled(true);
		
		graphNavigSpinner.setModel(new SpinnerNumberModel(currGrphIdx+1, 0, 
				dnGraphLibrary.size(), 1));
		((DefaultEditor) graphNavigSpinner.getEditor())
			.getTextField().setEditable(true); 
		((DefaultEditor) graphNavigSpinner.getEditor())
			.getTextField().setForeground(Color.BLACK);
		
		graphSpinnerListener.setEnabled(true);
	}
	
//-----------------------------------------------------------------------------
	
	private void protectEditedSystem()
	{
		btnSaveEdits.setEnabled(true);
		btnAddGraph.setEnabled(false);
		btnOpenGraphs.setEnabled(false);
		
		graphNavigSpinner.setModel(new SpinnerNumberModel(currGrphIdx+1, 
				currGrphIdx+1, currGrphIdx+1, 1));
		((DefaultEditor) graphNavigSpinner.getEditor())
			.getTextField().setEditable(false); 
		((DefaultEditor) graphNavigSpinner.getEditor())
			.getTextField().setForeground(Color.GRAY);
		
		graphSpinnerListener.setEnabled(false);
	}
	
//-----------------------------------------------------------------------------
    
    private void removeCurrentdnGraph() throws DENOPTIMException
    {
    	// Takes care of "dnGraph" and GUI components
    	clearCurrentSystem();
    	
    	// Actual removal from the library
    	if (dnGraphLibrary.size()>0)
    	{
    		dnGraphLibrary.remove(currGrphIdx);
    		int libSize = dnGraphLibrary.size();
    		
    		if (libSize > 0)
    		{
	    		if (currGrphIdx>=0 && currGrphIdx<libSize)
	    		{
	    			//we keep currGrphIdx as it will correspond to the next item
	    		}
	    		else
	    		{
	    			currGrphIdx = currGrphIdx-1;
	    		}
	
	    		// We use the currGrphIdx to load another dnGraph
		    	loadCurrentGraphIdxToViewer();
		    	updateGraphListSpinner();
    		}
    		else
    		{
    			currGrphIdx = -1;
    			//Spinner will be fixed by the deprotection routine
    			totalGraphsLabel.setText(Integer.toString(dnGraphLibrary.size()));
    		}
    		deprotectEditedSystem();
    	}
    }

//-----------------------------------------------------------------------------

  	private void saveUnsavedChanges() 
  	{	      		
  		// Overwrite dnGraph in library
  		dnGraphLibrary.set(currGrphIdx, dnGraph);
        
  		//TODO: check. this might not be needed
        // Reload dnGraph from library to refresh viewer
    	loadCurrentGraphIdxToViewer();
  		
  		// Release constraints
        deprotectEditedSystem();
  	}
  	
//-----------------------------------------------------------------------------

	/**
	 * Check whether there are unsaved changes.
	 * @return <code>true</code> if there are unsaved changes.
	 */
	
	public boolean hasUnsavedChanges()
	{
		return unsavedChanges;
	}
		
//-----------------------------------------------------------------------------
  	
}
