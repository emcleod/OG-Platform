/**
 * 
 */
package com.opengamma.examples.simulated.generator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.tool.ToolContext;

/**
 * @author emcleod
 * 
 * Utility for generating a list of securities with {@link ExternalSchemes#OG_SYNTHETIC_TICKER} tickers.
 */
public class SyntheticSecuritiesGeneratorTool extends AbstractSecuritiesGeneratorTool {

  @Override
  protected void configureChain(final SecurityGenerator<?> securityGenerator) {
    super.configureChain(securityGenerator);
    securityGenerator.setPreferredScheme(ExternalSchemes.OG_SYNTHETIC_TICKER);
  }

  /**
   * Main method. 
   * @param args Uses the command line args<p>
   * <ul>
   * <li> h - help
   * <li> c - config
   * <li> l - logback
   * <li> s - sets the name of the security generator (required)
   * <li> w - writes the portfolio and securities to the database
   * </ul>
   */
  public static void main(final String[] args) { // CSIGNORE
    final AbstractTool<ToolContext> tool = new AbstractTool<ToolContext>() {
      private final SyntheticSecuritiesGeneratorTool _instance = new SyntheticSecuritiesGeneratorTool();

      @Override
      protected Options createOptions(final boolean mandatoryConfigArg) {
        final Options options = super.createOptions(mandatoryConfigArg);
        _instance.createOptions(options);
        return options;
      }

      @Override
      protected void doRun() throws Exception {
        final CommandLine commandLine = getCommandLine();
        _instance.run(getToolContext(), commandLine);
      }

      @Override
      protected Class<?> getEntryPointClass() {
        return SyntheticSecuritiesGeneratorTool.class;
      }
    };
    tool.invokeAndTerminate(args);
  }

}
