import java.awt.Color;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Model {
	
	// the grid is the area in the ChartsRegion where users can position and size the charts
	final static int gridColumnsDefault = 8;
	final static int gridColumnsMinimum = 1;
	final static int gridColumnsMaximum = 20;
	static int gridColumns = gridColumnsDefault;
	
	final static int gridRowsDefault = 8;
	final static int gridRowsMinimum = 1;
	final static int gridRowsMaximum = 20;
	static int gridRows = gridRowsDefault;
	
	// serial port state
	static int sampleRate = 10000;
	static String packetType = "";
	static String portName = "";
	static int baudRate = 0;
	
	final static Color lineColorDefault = Color.RED;
	
	static float displayScalingFactor = (int) Math.round((double) Toolkit.getDefaultToolkit().getScreenResolution() / 100.0);
	final static float displayScalingFactorMinimum =  0.5f;
	final static float displayScalingFactorMaximum = 20.0f;
	
	// attributes of the possible charts
	static ChartDescriptor[] chartDescriptors = new ChartDescriptor[] {
		OpenGLTimeDomainChart.getDescriptor(),
		OpenGLTimeDomainChartCached.getDescriptor(),
		OpenGLFrequencyDomainChart.getDescriptor(),
		OpenGLHistogramChart.getDescriptor(),
		OpenGLDialChart.getDescriptor()
	};

	static Map<Integer, Dataset> datasets = Collections.synchronizedMap(new TreeMap<Integer, Dataset>());
	
	static List<PositionedChart> charts = Collections.synchronizedList(new ArrayList<PositionedChart>());
	
}
