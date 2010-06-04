package experimentalcode.heidi;

import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.visualization.VisualizationProjection;
import de.lmu.ifi.dbs.elki.visualization.css.CSSClass;
import de.lmu.ifi.dbs.elki.visualization.css.CSSClassManager.CSSNamingConflict;
import de.lmu.ifi.dbs.elki.visualization.svg.SVGPlot;
import de.lmu.ifi.dbs.elki.visualization.visualizers.Visualization;
import de.lmu.ifi.dbs.elki.visualization.visualizers.VisualizerContext;
import de.lmu.ifi.dbs.elki.visualization.visualizers.vis2d.Projection2DVisualizer;

/**
 * Generates an SVG-Element containing "dots" as markers representing the
 * selected Database's objects.
 * 
 * @author
 * 
 * @param <NV> Type of the NumberVector being visualized.
 */
public class SelectionDotVisualizerFactory<NV extends NumberVector<NV, ?>> extends Projection2DVisualizer<NV> {

  /**
   * A short name characterizing this Visualizer.
   */
  private static final String NAME = "Heidi SelectionDotVisualizer";

  /**
   * Generic tag to indicate the type of element. Used in IDs, CSS-Classes etc.
   */
  public static final String MARKER = "selectionDotMarker";

  Element layer;

  Element svgTag;

  SVGPlot svgp;

  VisualizationProjection proj;


    /**
     * Initializes this Visualizer.
     * 
     * @param context Visualization context
     */
    public void init(VisualizerContext<? extends NV> context) {
      super.init(NAME, context);
    }

    @Override
    public Visualization visualize(SVGPlot svgp, VisualizationProjection proj, double width, double height) {
      svgp.setDisableInteractions(true);
      return new SelectionDotVisualizer<NV>(context, svgp, proj, width, height);
    }

}
