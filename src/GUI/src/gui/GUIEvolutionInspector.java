package gui;


import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.List;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.editor.ChartEditor;
import org.jfree.chart.editor.ChartEditorManager;
import org.jfree.chart.entity.PlotEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

import denoptim.exception.DENOPTIMException;
import denoptim.io.DenoptimIO;
import denoptim.molecule.DENOPTIMMolecule;
import denoptim.utils.GenUtils;


/**
 * A panel that allows to inspect the output of an artificial evolution 
 * experiment. 
 * 
 * @author Marco Foscato
 */

public class GUIEvolutionInspector extends GUICardPanel
{
	/**
	 * Version UID
	 */
	private static final long serialVersionUID = -8303012362366503382L;
	
	/**
	 * Unique identified for instances of this handler
	 */
	public static AtomicInteger evolInspectorTabUID = new AtomicInteger(1);
	
	private JPanel ctrlPanel;
	private JMenu expMenu;
	private JMenu plotMenu;
	private JSplitPane centralPanel;
	private JPanel rightPanel;
	private MoleculeViewPanel molViewer;
	
	private File srcFolder;
	
	private ArrayList<DENOPTIMMolecule> allIndividuals;
	private int molsWithFitness = 0;
	private Map<Integer,DENOPTIMMolecule> candsWithFitnessMap;
	private Map<Integer,DENOPTIMMolecule> selectedMap;
	
	// WARNING: itemId in the data is "j" and is just a unique 
	// identifier that has NO RELATION to generation/molId/fitness
	// The Map 'candsWithFitnessMap' serve specifically to convert
	// the itemId 'j' into a DENOPTIMMolecule
	
	private DefaultXYDataset datasetAllFit;
	private DefaultXYDataset datasetSelected = new DefaultXYDataset();
	private DefaultXYDataset datasetPopMin = new DefaultXYDataset();	
	private DefaultXYDataset datasetPopMax = new DefaultXYDataset();
	private DefaultXYDataset datasetPopMean = new DefaultXYDataset();	
	private DefaultXYDataset datasetPopMedian = new DefaultXYDataset();
	private JFreeChart chart;
	private ChartPanel chartPanel;
	
	
//-----------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public GUIEvolutionInspector(GUIMainPanel mainPanel)
	{
		super(mainPanel, "Evolution Inspector #" 
					+ evolInspectorTabUID.getAndIncrement());
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
		
		// The Card has
		// -> its own toolbar
		// -> a central panel vertically divided in two
		//    |-> mol/graph viewers? (LEFT)
		//    |-> plot (RIGHT)
		
		// Creating local tool bar
		ctrlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ctrlPanel.add(new JLabel("Plot Features:"));
		JCheckBox ctrlMin = new JCheckBox("Minimum");
		ctrlMin.setToolTipText("The min fitness value in the population.");
		ctrlMin.setSelected(true);
		JCheckBox ctrlMax = new JCheckBox("Maximum");
		ctrlMax.setToolTipText("The max fitness value in the population.");
		ctrlMax.setSelected(true);
		JCheckBox ctrlMean = new JCheckBox("Mean");
		ctrlMean.setToolTipText("The mean fitness value in the population.");
		JCheckBox ctrlMedian = new JCheckBox("Median");
		ctrlMedian.setToolTipText("The median of the fitness in the "
				+ "population.");
		ctrlMin.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (ctrlMin.isSelected())
				{
					chart.getXYPlot().getRenderer(2).setSeriesVisible(0,true);
				}
				else
				{
					chart.getXYPlot().getRenderer(2).setSeriesVisible(0,false);
				}
			}
		});
		ctrlMax.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (ctrlMax.isSelected())
				{
					chart.getXYPlot().getRenderer(3).setSeriesVisible(0,true);
				}
				else
				{
					chart.getXYPlot().getRenderer(3).setSeriesVisible(0,false);
				}
			}
		});
		ctrlMean.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (ctrlMean.isSelected())
				{
					chart.getXYPlot().getRenderer(4).setSeriesVisible(0,true);
				}
				else
				{
					chart.getXYPlot().getRenderer(4).setSeriesVisible(0,false);
				}
			}
		});
		ctrlMedian.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (ctrlMedian.isSelected())
				{
					chart.getXYPlot().getRenderer(5).setSeriesVisible(0,true);
				}
				else
				{
					chart.getXYPlot().getRenderer(5).setSeriesVisible(0,false);
				}
			}
		});
		ctrlPanel.add(ctrlMin);
		ctrlPanel.add(ctrlMax);
		ctrlPanel.add(ctrlMean);
		ctrlPanel.add(ctrlMedian);
		JButton rstView = new JButton("Reser Chart View");
		rstView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chart.getXYPlot().getDomainAxis().setAutoRange(true);
				chart.getXYPlot().getRangeAxis().setAutoRange(true);
				chart.getXYPlot().getDomainAxis().setLowerBound(-0.5);			
			}
		});
		ctrlPanel.add(rstView);
		this.add(ctrlPanel,BorderLayout.NORTH);
		
		// Setting structure of central panel	
		centralPanel = new JSplitPane();
		centralPanel.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		centralPanel.setOneTouchExpandable(true);
		rightPanel = new JPanel(); 
		rightPanel.setLayout(new BorderLayout());
		centralPanel.setRightComponent(rightPanel);
		molViewer = new MoleculeViewPanel();
		centralPanel.setLeftComponent(molViewer);
		centralPanel.setDividerLocation(300);
		this.add(centralPanel,BorderLayout.CENTER);

		// Button to the bottom of the card
		JPanel commandsPane = new JPanel();
		this.add(commandsPane, BorderLayout.SOUTH);
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
		
	}
	
//-----------------------------------------------------------------------------

	public void importEvolutionRunData(File file) {

		mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		srcFolder = file;
		
		//TODO del o log?
		System.out.println("Importing data from '" + srcFolder + "'...");
		
		Map<Integer,double[]> popPropertiess = new HashMap<Integer,double[]>();
		allIndividuals = new ArrayList<DENOPTIMMolecule>();
		for (File genFolder : file.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().startsWith("Gen")
						&& pathname.isDirectory())
				{
					return true;
				}
				return false;
			}
		}))
		{			
			//WARNING: assuming folders are named "Gen.*"
			int genId = Integer.parseInt(genFolder.getName().substring(3));
			int padSize = genFolder.getName().substring(3).length();
			
			String zeroedGenId = GenUtils.getPaddedString(padSize,genId);
			
			// Read Generation summaries
			File genSummary = new File(genFolder 
					+ System.getProperty("file.separator") 
					+ "Gen" + zeroedGenId + ".txt");
			
			//TODO del o log?
			System.out.println("Reading "+genSummary);
			
			if (!DenoptimIO.checkExists(genSummary.getAbsolutePath()))
			{
				JOptionPane.showMessageDialog(null,
		                "<html>File '" + genSummary + "' not found!<br>"
		                + "There will be holes in the min/max/mean profile."
		                + "</html>",
		                "Error",
		                JOptionPane.PLAIN_MESSAGE,
		                UIManager.getIcon("OptionPane.errorIcon"));
			}
			try {
				popPropertiess.put(genId, DenoptimIO.readPopulationProps(
						genSummary));
			} catch (DENOPTIMException e2) {
				JOptionPane.showMessageDialog(null,
		                "<html>File '" + genSummary + "' not found!<br>"
		                + "There will be holes in the min/max/mean profile."
		                + "</html>",
		                "Error",
		                JOptionPane.PLAIN_MESSAGE,
		                UIManager.getIcon("OptionPane.errorIcon"));
			}
			
			
			// Read DENOPTIMMolecules
			for (File fitFile : genFolder.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
					if (pathname.getName().endsWith("FIT.sdf"))
					{
						return true;
					}
					return false;
				}
			}))
			{
				DENOPTIMMolecule one;
				try {
					one = DenoptimIO.readDENOPTIMMolecules(
							fitFile,false).get(0);
				} catch (DENOPTIMException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null,
			                "Could not read data from to '" + fitFile + "'!.",
			                "Error",
			                JOptionPane.PLAIN_MESSAGE,
			                UIManager.getIcon("OptionPane.errorIcon"));
					return;
				}
				if (one.hasFitness())
				{
					molsWithFitness++;
				}
				one.setGeneration(genId);
				allIndividuals.add(one);
			}
		}
		
		//TODO del o log?
		System.out.println("Imported "+allIndividuals.size()+" individuals.");
		
		// Process data and organize then into series for the plot
		datasetAllFit = new DefaultXYDataset(); 
        double[][] candsWithFitnessData = new double[2][molsWithFitness];
        candsWithFitnessMap = new HashMap<Integer,DENOPTIMMolecule>();
        int j = -1;
        for (int i=0; i<allIndividuals.size(); i++)
        {
        	DENOPTIMMolecule mol = allIndividuals.get(i);
        	if (!mol.hasFitness())
        	{
        		continue;
        	}
        	
        	// WARNING: itemId in the data is "j" and is just a unique 
        	// identifier that has NO RELATION to generation/molId/fitness
        	// The Map 'candsWithFitnessMap' serve specifically to convert
        	// the itemId 'j' into a DENOPTIMMolecule
        	
        	j++;
        	candsWithFitnessMap.put(j, mol);
        	candsWithFitnessData[0][j] = mol.getGeneration();
        	candsWithFitnessData[1][j] = mol.getMoleculeFitness();
        }
		datasetAllFit.addSeries("Candidates_with_fitness", candsWithFitnessData);
		
		int numGen = popPropertiess.keySet().size();
		double[][] popMin = new double[2][numGen];
		double[][] popMax = new double[2][numGen];
		double[][] popMean = new double[2][numGen];
		double[][] popMedian = new double[2][numGen];
		for (int i=0; i<numGen; i++)
		{
			double[] values = popPropertiess.get(i);			
			popMin[0][i] = i;
			popMin[1][i] = values[0];
			popMax[0][i] = i;
			popMax[1][i] = values[1];
			popMean[0][i] = i;
			popMean[1][i] = values[2];
			popMedian[0][i] = i;
			popMedian[1][i] = values[3];
			
		}
		datasetPopMin.addSeries("Population_min", popMin);
		datasetPopMax.addSeries("Population_max", popMax);
		datasetPopMean.addSeries("Population_mean", popMean);
		datasetPopMedian.addSeries("Population_median", popMedian);
		
		
		//TODO: somehow collect and display the candidates that hit a mol error
		//      Could it be a histograpm (#failed x gen) below theevolution plot
		
		chart = ChartFactory.createScatterPlot(
	            null,                         // plot title
	            "Generation",                 // x axis label
	            "Fitness",                    // y axis label
	            datasetAllFit,                // all items with fitness
	            PlotOrientation.VERTICAL,  
	            false,                        // include legend
	            false,                        // tool tips
	            false                         // urls
	        );
		
		// Chart appearance
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.getDomainAxis().setLowerBound(-0.5); //min X-axis
		plot.setBackgroundPaint(Color.WHITE);
		plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		plot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		// main dataset (all items with fitness)
		Shape shape0 = new Ellipse2D.Double(
				 -GUIPreferences.chartPointSize/2.0,
	             -GUIPreferences.chartPointSize/2.0,
	             GUIPreferences.chartPointSize, 
	             GUIPreferences.chartPointSize);
		XYLineAndShapeRenderer renderer0 = 
				(XYLineAndShapeRenderer) plot.getRenderer();
        renderer0.setSeriesShape(0, shape0);
        renderer0.setSeriesPaint(0, Color.decode("#848482"));
        renderer0.setSeriesOutlinePaint(0, Color.gray);
        
        // dataset of selected items
		Shape shape1 = new Ellipse2D.Double(
				 -GUIPreferences.chartPointSize*1.1/2.0,
	             -GUIPreferences.chartPointSize*1.1/2.0,
	             GUIPreferences.chartPointSize*1.1, 
	             GUIPreferences.chartPointSize*1.1);
        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
        //now the dataset of selected is null. Created upon selection
        //plot.setDataset(1, datasetSelected); 
        plot.setRenderer(1, renderer1);
        renderer1.setSeriesShape(0, shape1);
        renderer1.setSeriesPaint(0, Color.red);
        renderer1.setSeriesFillPaint(0, Color.red);
        renderer1.setSeriesOutlinePaint(0, Color.BLACK);
        renderer1.setUseOutlinePaint(true);
        renderer1.setUseFillPaint(true);
        
        // min fitness in the population
        XYLineAndShapeRenderer renderer2 = 
        		new XYLineAndShapeRenderer(true, false);
        plot.setDataset(2, datasetPopMin);
        plot.setRenderer(2, renderer2);
        renderer2.setSeriesPaint(0, Color.blue);
        
        // max fitness in the population
        XYLineAndShapeRenderer renderer3 = 
        		new XYLineAndShapeRenderer(true, false);
        plot.setDataset(3, datasetPopMax);
        plot.setRenderer(3, renderer3);
        renderer3.setSeriesPaint(0, Color.blue);
        
        // mean fitness in the population
        XYLineAndShapeRenderer renderer4 = 
        		new XYLineAndShapeRenderer(true, false);
        plot.setDataset(4, datasetPopMean);
        plot.setRenderer(4, renderer4);
        renderer4.setSeriesPaint(0, Color.red);
        renderer4.setSeriesStroke(0, new BasicStroke(
                2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 
                1.0f, new float[] {10.0f, 6.0f}, 0.0f));
        renderer4.setSeriesVisible(0, false);
        
        // median fitness in the population
        XYLineAndShapeRenderer renderer5 = 
        		new XYLineAndShapeRenderer(true, false);
        plot.setDataset(5, datasetPopMedian);
        plot.setRenderer(5, renderer5);
        renderer5.setSeriesPaint(0, Color.decode("#22BB22"));   
        renderer5.setSeriesStroke(0, new BasicStroke(
                    2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 
                    1.0f, new float[] {3.0f, 6.0f}, 0.0f));
        renderer5.setSeriesVisible(0, false);
        
		// Create the actual panel that contains the chart
		chartPanel = new ChartPanel(chart);
		
		// Adapt chart size to the size of the panel
		rightPanel.addComponentListener(new ComponentAdapter() {
	        @Override
	        public void componentResized(ComponentEvent e) {
	        	chartPanel.setMaximumDrawHeight(e.getComponent().getHeight());
	        	chartPanel.setMaximumDrawWidth(e.getComponent().getWidth());
	        	chartPanel.setMinimumDrawWidth(e.getComponent().getWidth());
	        	chartPanel.setMinimumDrawHeight(e.getComponent().getHeight());
	        	// WARNING: this update is needed to make the new size affective
	        	// also after movement of the JSplitPane divider, which is
	        	// otherwise felt by this listener but the new sizes do not
	        	// take effect. 
	        	ChartEditor ce = ChartEditorManager.getChartEditor(chart);
	        	ce.updateChart(chart);
	        }
	    });
		
		// Setting toolTip when on top of an series item in the chart
		XYToolTipGenerator ttg = new XYToolTipGenerator() {
			
			@Override
			public String generateToolTip(XYDataset data, int sId, int itemId)
			{
				return candsWithFitnessMap.get(itemId).getName();
			}
		};
		chart.getXYPlot().getRenderer().setSeriesToolTipGenerator(0, ttg);
		
		// Clock-based selection of item, possibly displaying mol structure
		chartPanel.addChartMouseListener(new ChartMouseListener() {
			
			@Override
			public void chartMouseMoved(ChartMouseEvent e) {
				//nothing to do
			}
			
			@Override
			public void chartMouseClicked(ChartMouseEvent e) 
			{
				System.out.println("entity: "+e.getEntity());
				if (e.getEntity() instanceof XYItemEntity)
				{
					int serId = ((XYItemEntity)e.getEntity()).getSeriesIndex();
					Map<Integer,DENOPTIMMolecule>  seriesMap = 
							new HashMap<Integer,DENOPTIMMolecule>();
					if (serId == 0)
					{
						seriesMap = candsWithFitnessMap;
					
						int itemId = ((XYItemEntity) e.getEntity()).getItem();
						DENOPTIMMolecule mol = seriesMap.get(itemId);
						renderViewWithSelectedItem(mol);
					}
					//do we do anything if we select other series? not now...
				}
				else if (e.getEntity() instanceof PlotEntity)
				{
					renderViewWithoutSelectedItems();
				}
			}
		});
		
		rightPanel.add(chartPanel,BorderLayout.CENTER);
		
		mainPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
//-----------------------------------------------------------------------------
	
	private void renderViewWithSelectedItem(DENOPTIMMolecule mol)
	{
		// Update series of selected (chart is updated automatically)
        double[][] selectedCandsData = new double[2][1]; //NB: for now allow only one
        selectedMap = new HashMap<Integer,DENOPTIMMolecule>();
        int j = -1;
        for (int i=0; i<1; i++) //NB: for now allow only one
        {	
        	j++;
        	candsWithFitnessMap.put(j, mol);
        	selectedCandsData[0][j] = mol.getGeneration();
        	selectedCandsData[1][j] = mol.getMoleculeFitness();
        }
        datasetSelected.removeSeries("Selected_candidates");
        datasetSelected.addSeries("Selected_candidates", selectedCandsData);
		chart.getXYPlot().setDataset(1, datasetSelected);
		
		// Update the molecular viewer
		molViewer.loadMolecularFromFile(mol.getMoleculeFile());
	}
	
//-----------------------------------------------------------------------------
	
	private void renderViewWithoutSelectedItems()
	{
		datasetSelected.removeSeries("Selected_candidates");
		molViewer.clearAll();
	}
	
//-----------------------------------------------------------------------------
  	
}
