import java.util.Map;

import com.jogamp.opengl.GL2;

@SuppressWarnings("serial")
public class OpenGLTimeDomainChart extends PositionedChart {
	
	OpenGLTimeDomainSlice slice;
	AutoScale autoscale;
	
	public static ChartDescriptor getDescriptor() {
		
		return new ChartDescriptor() {
			
			@Override public String toString()        { return "Time Domain Chart"; }
			@Override public int getMinimumDuration() { return 5; }
			@Override public int getDefaultDuration() { return 1000; }
			@Override public int getMaximumDuration() { return Integer.MAX_VALUE; }
			@Override public String[] getInputNames() { return null; }
			
			@Override public PositionedChart createChart(int x1, int y1, int x2, int y2, int chartDuration, Dataset[] chartInputs) {
				return new OpenGLTimeDomainChart(x1, y1, x2, y2, chartDuration, chartInputs);
			}
			
		};
		
	}
	
	@Override public String toString() {
		
		return "Time Domain Chart";
		
	}
	
	public OpenGLTimeDomainChart(int x1, int y1, int x2, int y2, int chartDuration, Dataset[] chartInputs) {
		
		super(x1, y1, x2, y2, chartDuration, chartInputs);
		
		autoscale = new AutoScale(AutoScale.MODE_EXPONENTIAL, 30, 0.10f);

	}
	
	@Override public void drawChart(GL2 gl, int width, int height, int lastSampleNumber, double zoomLevel) {
		
		// draw background
		gl.glBegin(GL2.GL_QUADS);
		gl.glColor4fv(Theme.backgroundColor, 0);
			gl.glVertex2f(0,     0);
			gl.glVertex2f(0,     height);
			gl.glVertex2f(width, height);
			gl.glVertex2f(width, 0);
		gl.glEnd();
		
		// draw perimeter outline
		gl.glBegin(GL2.GL_LINE_LOOP);
		gl.glColor4fv(Theme.perimeterOutlineColor, 0);
			gl.glVertex2f(0,     0);
			gl.glVertex2f(0,     height);
			gl.glVertex2f(width, height);
			gl.glVertex2f(width, 0);
		gl.glEnd();
		
		// calculate domain
		int plotMaxX = lastSampleNumber;
		int plotMinX = plotMaxX - (int) (duration * zoomLevel) + 1;
		int minDomain = getDescriptor().getMinimumDuration() - 1;
		if(plotMaxX - plotMinX < minDomain) plotMinX = plotMaxX - minDomain;
		float domain = plotMaxX - plotMinX;
		
		if(plotMaxX < minDomain)
			return;
		
		// get the samples
		if(slice == null) {
			slice = new OpenGLTimeDomainSlice(gl);
			slice.freeResources(gl);
		}
		slice.updateSamples(datasets, plotMinX > 0 ? plotMinX : 0, plotMaxX);
		
		// ensure range is >0
		float plotMaxY = slice.sliceMaxY;
		float plotMinY = slice.sliceMinY;
		if(plotMinY == plotMaxY) {
			float value = plotMinY;
			plotMinY = value - 0.001f;
			plotMaxY = value + 0.001f;
		}
		autoscale.update(plotMinY, plotMaxY);
		plotMaxY = autoscale.getMax();
		plotMinY = autoscale.getMin();
		float plotRange = plotMaxY - plotMinY;
		
		// calculate x and y positions of everything
		String xAxisTitle = "Sample Number";
		String yAxisTitle = datasets[0].unit;
		float xLegendBorderLeft = Theme.perimeterPadding;
		float yLegendBorderBottom = Theme.perimeterPadding;
		float yLegendTextBaseline = yLegendBorderBottom + Theme.legendTextPadding;
		float yLegendTextTop = yLegendTextBaseline + FontUtils.legendTextHeight;
		float yLegendBorderTop = yLegendTextTop + Theme.legendTextPadding;
		
		float yXaxisTitleTextBasline = Theme.perimeterPadding;
		float yXaxisTitleTextTop = yXaxisTitleTextBasline + FontUtils.xAxisTextHeight;
		
		float yXaxisTickTextBaseline = Float.max(yLegendBorderTop + Theme.legendTextPadding, yXaxisTitleTextTop + Theme.legendTextPadding);
		float yXaxisTickTextTop = yXaxisTickTextBaseline + FontUtils.tickTextHeight;
		float yXaxisTickBottom = yXaxisTickTextTop + Theme.tickTextPadding;
		float yXaxisTickTop = yXaxisTickBottom + Theme.tickLength;
		
		float yPlotBottom = yXaxisTickTop;
		float yPlotTop = height - Theme.perimeterPadding;
		float plotHeight = yPlotTop - yPlotBottom;
		if(plotHeight < 1.0f)
			return;
		
		Map<Float, String> yDivisions = ChartUtils.getYdivisions125(plotHeight, plotMinY, plotMaxY);
		
		float maxYtickTextWidth = 0;
		for(String text : yDivisions.values()) {
			float w = FontUtils.tickTextWidth(text); 
			if(w > maxYtickTextWidth)
				maxYtickTextWidth = w;
		}
		
		float xYaxisTitleTextTop = Theme.perimeterPadding;
		float xYaxisTitleTextBaseline = xYaxisTitleTextTop + FontUtils.yAxisTextHeight;
		
		float xYaxisTickTextLeft = xYaxisTitleTextBaseline + Theme.tickTextPadding;
		float xYaxisTickTextRight = xYaxisTickTextLeft + maxYtickTextWidth;
		float xYaxisTickLeft = xYaxisTickTextRight + Theme.tickTextPadding;
		float xYaxisTickRight = xYaxisTickLeft + Theme.tickLength;
		
		float xPlotLeft = xYaxisTickRight;
		float xPlotRight = width - Theme.perimeterPadding;
		float plotWidth = xPlotRight - xPlotLeft;
		if(plotWidth < 1.0f)
			return;
		
		Map<Integer, String> xDivisions = ChartUtils.getXdivisions125(plotWidth, plotMinX, plotMaxX);
		
		float xXaxisTitleLeft = xPlotLeft +   (plotWidth  / 2.0f) - (FontUtils.xAxisTextWidth(xAxisTitle)  / 2.0f);
		float yYaxisTitleLeft = yPlotBottom + (plotHeight / 2.0f) - (FontUtils.yAxisTextWidth(yAxisTitle) / 2.0f);
		
		// draw plot background
		gl.glBegin(GL2.GL_QUADS);
		gl.glColor4fv(Theme.plotBackgroundColor, 0);
			gl.glVertex2f(xPlotLeft,  yPlotTop);
			gl.glVertex2f(xPlotRight, yPlotTop);
			gl.glVertex2f(xPlotRight, yPlotBottom);
			gl.glVertex2f(xPlotLeft,  yPlotBottom);
		gl.glEnd();
		
		// draw the vertical division lines
		gl.glBegin(GL2.GL_LINES);
		for(Integer xValue : xDivisions.keySet()) {
			float x = (float) (xValue - plotMinX) / domain * plotWidth + xPlotLeft;
			gl.glColor4fv(Theme.divisionLinesColor, 0);
			gl.glVertex2f(x, yPlotTop);
			gl.glVertex2f(x, yPlotBottom);
			gl.glColor4fv(Theme.tickLinesColor, 0);
			gl.glVertex2f(x, yXaxisTickTop);
			gl.glVertex2f(x, yXaxisTickBottom);
		}
		gl.glEnd();
		
		// draw the vertical division line text
		for(Map.Entry<Integer,String> entry : xDivisions.entrySet()) {
			float x = (float) (entry.getKey() - plotMinX) / domain * plotWidth + xPlotLeft - (FontUtils.tickTextWidth(entry.getValue()) / 2.0f);
			float y = yXaxisTickTextBaseline;
			FontUtils.drawTickText(entry.getValue(), (int) x, (int) y);
		}
		
		// draw the horizontal division lines
		gl.glBegin(GL2.GL_LINES);
		for(Float entry : yDivisions.keySet()) {
			float y = (entry - plotMinY) / plotRange * plotHeight + yPlotBottom;
			gl.glColor4fv(Theme.divisionLinesColor, 0);
			gl.glVertex2f(xPlotLeft,  y);
			gl.glVertex2f(xPlotRight, y);
			gl.glColor4fv(Theme.tickLinesColor, 0);
			gl.glVertex2f(xYaxisTickLeft,  y);
			gl.glVertex2f(xYaxisTickRight, y);
		}
		gl.glEnd();
		
		// draw the horizontal division line text
		for(Map.Entry<Float,String> entry : yDivisions.entrySet()) {
			float x = xYaxisTickTextRight - FontUtils.tickTextWidth(entry.getValue());
			float y = (entry.getKey() - plotMinY) / plotRange * plotHeight + yPlotBottom - (FontUtils.tickTextHeight / 2.0f);
			FontUtils.drawTickText(entry.getValue(), (int) x, (int) y);
		}
		
		// draw the legend
		float xOffset = xLegendBorderLeft + Theme.strokeWidth + Theme.legendTextPadding;
		for(SamplesGL dataset : slice.glDataset) {
			gl.glBegin(GL2.GL_QUADS);
			gl.glColor4fv(dataset.color, 0);
				gl.glVertex2f(xOffset,                              yLegendTextBaseline);
				gl.glVertex2f(xOffset,                              yLegendTextTop);
				gl.glVertex2f(xOffset + FontUtils.legendTextHeight, yLegendTextTop);
				gl.glVertex2f(xOffset + FontUtils.legendTextHeight, yLegendTextBaseline);
			gl.glEnd();
			
			xOffset += FontUtils.legendTextHeight + Theme.legendTextPadding;
			
			FontUtils.drawLegendText(dataset.name, (int) xOffset, (int) yLegendTextBaseline);
			xOffset += FontUtils.legendTextWidth(dataset.name) + Theme.legendNamesPadding;
		}
		xOffset -= Theme.legendNamesPadding;
		gl.glBegin(GL2.GL_LINE_LOOP);
		gl.glColor4fv(Theme.legendOutlineColor, 0);
			gl.glVertex2f(xLegendBorderLeft,           yLegendBorderBottom);
			gl.glVertex2f(xLegendBorderLeft,           yLegendBorderTop);
			gl.glVertex2f(xLegendBorderLeft + xOffset, yLegendBorderTop);
			gl.glVertex2f(xLegendBorderLeft + xOffset, yLegendBorderBottom);
		gl.glEnd();
		
		// draw the x-axis title, shifting it to the right if the legend gets in the way
		{
			float x = xXaxisTitleLeft > xOffset + Theme.legendTextPadding ? xXaxisTitleLeft : xLegendBorderLeft + xOffset + Theme.legendTextPadding;
			float y = yXaxisTitleTextBasline;
			FontUtils.drawXaxisText(xAxisTitle, (int) x, (int) y);
		}
		
		// draw the y-axis title, if space is available
		if(yYaxisTitleLeft >= yPlotBottom) {
			float x = xYaxisTitleTextBaseline;
			float y = yYaxisTitleLeft;
			FontUtils.drawYaxisText(yAxisTitle, (int) x, (int) y, 90);
		}
		
		// draw each dataset
		if(slice.glDataset[0].vertexCount >= 2) {
			for(int i = 0; i < slice.glDataset.length; i++) {
				
				gl.glMatrixMode(GL2.GL_MODELVIEW);
				gl.glPushMatrix();
				
				// adjust so: x = (x - plotMinX) / domain * plotWidth + xPlotLeft;
				gl.glTranslatef(xPlotLeft, 0, 0);
				gl.glScalef(plotWidth, 1, 1);
				gl.glScalef(1.0f / domain, 1, 1);
				gl.glTranslatef(-plotMinX, 0, 0);
				
				// adjust so y = (y - plotMinY) / plotRange * plotHeight + yPlotBottom;
				gl.glTranslatef(0, yPlotBottom, 0);
				gl.glScalef(1, plotHeight, 1);
				gl.glScalef(1, 1.0f / plotRange, 1);
				gl.glTranslatef(0, -plotMinY, 0);
				
				gl.glColor4fv(slice.glDataset[i].color, 0);
				gl.glVertexPointer(2, GL2.GL_FLOAT, 0, slice.glDataset[i].buffer);
				gl.glDrawArrays(GL2.GL_LINE_STRIP, 0, slice.glDataset[i].vertexCount);
				
				gl.glPopMatrix();
				
			}
		}
		
		// draw the plot border
		gl.glBegin(GL2.GL_LINE_LOOP);
		gl.glColor4fv(Theme.plotOutlineColor, 0);
			gl.glVertex2f(xPlotLeft,  yPlotTop);
			gl.glVertex2f(xPlotRight, yPlotTop);
			gl.glVertex2f(xPlotRight, yPlotBottom);
			gl.glVertex2f(xPlotLeft,  yPlotBottom);
		gl.glEnd();
		
	}

}
