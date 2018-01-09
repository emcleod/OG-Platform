/**
 * 
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.SelectorResolver;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

public class CurveBundleSelector implements DistinctMarketDataSelector {
  private final Set<String> _curveConstructionConfigNames;
  private final Set<String> _names;
  private final PatternWrapper _nameMatchPattern;
  private final PatternWrapper _nameLikePattern;

  protected CurveBundleSelector(final Set<String> curveConstructionConfigNames, final Set<String> names,
      final Pattern nameMatchPattern, final Pattern nameLikePattern) {
    _curveConstructionConfigNames = curveConstructionConfigNames;
    _names = names;
    _nameMatchPattern = PatternWrapper.wrap(nameMatchPattern);
    _nameLikePattern = PatternWrapper.wrap(nameLikePattern);
  }

  protected Set<String> getCurveBundleNames() {
    return _curveConstructionConfigNames;
  }

  protected Set<String> getNames() {
    return _names;
  }

  protected Pattern getNameMatchPattern() {
    return _nameMatchPattern == null ? null : _nameMatchPattern.getPattern();
  }

  protected Pattern getNameLikePattern() {
    return _nameLikePattern == null ? null : _nameLikePattern.getPattern();
  }

  protected boolean matches(final ValueSpecification valueSpecification) {
    ArgumentChecker.notNull(valueSpecification, "valueSpecification");
    if (!CURVE_BUNDLE.equals(valueSpecification.getValueName())) {
      return false;
    }
    final String curveConstructionConfigName = valueSpecification.getProperties().getStrictValue(CURVE_CONSTRUCTION_CONFIG);
    if (curveConstructionConfigName == null) {
      return false;
    }
    final Set<String> curveNames = valueSpecification.getProperties().getValues(CURVE);
    if (curveNames == null) {
      return false;
    }
    return matches(curveConstructionConfigName, curveNames);
  }

  protected boolean matches(final String curveConstructionConfigName, final Set<String> curveNames) {
    ArgumentChecker.notNull(curveConstructionConfigName, "curveConstructionConfigName");
    ArgumentChecker.notEmpty(curveNames, "curveNames");
    if (_curveConstructionConfigNames != null && !_curveConstructionConfigNames.contains(curveConstructionConfigName)) {
      return false;
    }
    if (!_names.containsAll(curveNames)) {
      return false;
    }
    boolean matches = true;
    for (final String curveName : curveNames) {
      if (_nameMatchPattern != null && !_nameMatchPattern.getPattern().matcher(curveName).matches()) {
        if (_nameLikePattern != null && !_nameLikePattern.getPattern().matcher(curveName).matches()) {
          matches = false;
        }
      }
    }
    return matches;
  }

  @Override
  public boolean hasSelectionsDefined() {
    return true;
  }

  @Override
  public DistinctMarketDataSelector findMatchingSelector(final ValueSpecification valueSpecification, final String curveConstructionConfigName,
      final SelectorResolver resolver) {
    ArgumentChecker.notNull(valueSpecification, "valueSpecification");
    if (_curveConstructionConfigNames != null && !_curveConstructionConfigNames.contains(curveConstructionConfigName)) {
      return null;
    }
    if (matches(valueSpecification)) {
      return this;
    }
    return null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_curveConstructionConfigNames, _names, _nameLikePattern, _nameMatchPattern);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CurveBundleSelector)) {
      return false;
    }
    final CurveBundleSelector other = (CurveBundleSelector) obj;
    return Objects.equals(_curveConstructionConfigNames, other._curveConstructionConfigNames) &&
        Objects.equals(_names, other._names) &&
        Objects.equals(_nameLikePattern, other._nameLikePattern) &&
        Objects.equals(_nameMatchPattern, other._nameMatchPattern);
  }

  @Override
  public String toString() {
    return "CurveBundleSelector [" +
        "_curveConstructionConfigNames=" + _curveConstructionConfigNames +
        "_curveNames=" + _names +
        "_nameMatchPattern=" + _nameMatchPattern +
        "_nameLikePattern=" + _nameLikePattern +
        "]";
  }

  public static class Builder {
    private final CurveBundleScenario _scenario;
    private Set<String> _curveConstructionConfigNames;
    private Set<String> _names;
    private Pattern _nameMatchPattern;
    private Pattern _nameLikePattern;

    protected Builder(final CurveBundleScenario scenario) {
      ArgumentChecker.notNull(scenario, "scenario");
      _scenario = scenario;
    }

    protected Builder curveBundleNamed(final String... names) {
      ArgumentChecker.notEmpty(names, "names");
      if (_curveConstructionConfigNames != null) {
        throw new IllegalStateException("curveBundleNamed() can only be called once");
      }
      _curveConstructionConfigNames = ImmutableSet.copyOf(names);
      return this;
    }

    protected Builder named(final String... names) {
      ArgumentChecker.notEmpty(names, "names");
      if (_names != null) {
        throw new IllegalStateException("named() can only be called once");
      }
      if (_nameMatchPattern != null) {
        throw new IllegalStateException("Only one of named() and nameMatches() can be used");
      }
      if (_nameLikePattern != null) {
        throw new IllegalStateException("Only one of named() and nameLike() can be used");
      }
      _names = ImmutableSet.copyOf(names);
      return this;
    }

    protected Builder nameMatches(final String regex) {
      if (_nameMatchPattern != null) {
        throw new IllegalStateException("nameMatches() can only be called once");
      }
      if (_nameLikePattern != null) {
        throw new IllegalStateException("Only one of nameMatches() and nameLike() can be used");
      }
      if (_names != null) {
        throw new IllegalStateException("Only one of named(), nameMatches() and nameLike() can be used");
      }
      _nameMatchPattern = Pattern.compile(regex);
      return this;
    }

    protected Builder nameLike(final String glob) {
      if (_nameLikePattern != null) {
        throw new IllegalStateException("namelike() can only be called once");
      }
      if (_nameMatchPattern != null) {
        throw new IllegalStateException("Only one of nameMatches() and nameLike() can be used");
      }
      if (_names != null) {
        throw new IllegalStateException("Only one of named(), nameMatches() and nameLike() can be used");
      }
      _nameMatchPattern = SimulationUtils.patternForGlob(glob);
      return this;
    }

    protected CurveBundleScenario getScenario() {
      return _scenario;
    }

    protected Set<String> getCurveConstructionConfigNames() {
      return _curveConstructionConfigNames;
    }

    protected Set<String> getNames() {
      return _names;
    }

    protected Pattern getNameMatchPattern() {
      return _nameMatchPattern;
    }

    protected Pattern getNameLikePattern() {
      return _nameLikePattern;
    }
  }
}
