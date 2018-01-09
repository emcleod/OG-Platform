/**
 * 
 */
package com.opengamma.examples.simulated.tool;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.bbg.referencedata.impl.AbstractReferenceDataProvider;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.index.IborIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.master.convention.impl.InMemoryConventionMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.InMemoryHistoricalTimeSeriesMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

public class CurveUnderlyingsLoaderTest {

  private static final String DATA_FIELD = "PX_LAST";
  private MyReferenceDataProvider _referenceDataProvider;

  @BeforeMethod
  public void setup() {
    _referenceDataProvider = new MyReferenceDataProvider();
    _referenceDataProvider.addResult("USDP3MLIBOR", BloombergConstants.FIELD_SECURITY_DES, "3m LIBOR USD");
    _referenceDataProvider.addResult("USDP3MLIBOR", BloombergConstants.FIELD_PARSEKYABLE_DES, "3m LIBOR USD");
    _referenceDataProvider.addResult("USDP3MLIBOR", BloombergConstants.FIELD_INDX_SOURCE, "ICE");
    _referenceDataProvider.addResult("USDP3MLIBOR", BloombergConstants.FIELD_ID_BBG_UNIQUE, "USDP3MLIBOR");
    _referenceDataProvider.addResult("USDP3MLIBOR", BloombergConstants.FIELD_ID_CUSIP, "000000001");
    _referenceDataProvider.addResult("USDP3MLIBOR", BloombergConstants.FIELD_ID_ISIN, "00000000001");
    _referenceDataProvider.addResult("USDP3MLIBOR", BloombergConstants.FIELD_ID_SEDOL1, "000000001");
    _referenceDataProvider.addExpectedField(BloombergConstants.FIELD_SECURITY_DES);
    _referenceDataProvider.addExpectedField(BloombergConstants.FIELD_PARSEKYABLE_DES);
    _referenceDataProvider.addExpectedField(BloombergConstants.FIELD_INDX_SOURCE);
    _referenceDataProvider.addExpectedField(BloombergConstants.FIELD_ID_BBG_UNIQUE);
    _referenceDataProvider.addExpectedField(BloombergConstants.FIELD_ID_CUSIP);
    _referenceDataProvider.addExpectedField(BloombergConstants.FIELD_ID_ISIN);
    _referenceDataProvider.addExpectedField(BloombergConstants.FIELD_ID_SEDOL1);
  }

  @Test
  public void testUnderlyingsForSwap() {
    //fixed / ibor
    //fixed / overnight
    //fixed / CMS
    //float / float

  }

  @Test
  public void testUnderlyingsForInterestRateSwap() {
    //fixed / ibor
    //fixed / overnight
    //fixed / CMS
    //float / float

  }

  @Test
  public void testUnderlyingsForFra() {
    final ExternalId underlyingId = ExternalSchemes.syntheticSecurityId("USDP3MLIBOR");
    final ExternalId calendarId = ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2.getName(), "US");
    final ManageableSecurity fra = new FRASecurity(Currency.USD, calendarId,
        LocalDate.of(2014, 6, 1).atStartOfDay(ZoneOffset.UTC), LocalDate.of(2014, 9, 1).atStartOfDay(ZoneOffset.UTC),
        0.001, 100, underlyingId, LocalDate.of(2014, 9, 1).atStartOfDay(ZoneOffset.UTC));
    fra.setExternalIdBundle(ExternalIdBundle.of("MemSec", "fra"));
    final ExternalId conventionId = ExternalId.of("MemCnv", "LIBOR USD");
    final ManageableConvention convention = new IborIndexConvention("LIBOR USD", conventionId.toBundle(), DayCounts.ACT_360,
        BusinessDayConventions.FOLLOWING, 2, false, Currency.USD, LocalTime.of(11, 0), "Eastern", calendarId, calendarId, "");
    final ManageableSecurity iborIndex = new IborIndex("LIBOR USD 3m", "3m USD Libor", Tenor.ofMonths(3), conventionId);
    iborIndex.setExternalIdBundle(ExternalIdBundle.of("MemSec", "ibor"));
    final LocalDateDoubleTimeSeries iborTs = ImmutableLocalDateDoubleTimeSeries.of(LocalDate.of(2014, 6, 1), 0.01);

    final SecurityMaster securityMaster = new InMemorySecurityMaster();
    final HistoricalTimeSeriesMaster htsMaster = new InMemoryHistoricalTimeSeriesMaster();
    final ConventionMaster conventionMaster = new InMemoryConventionMaster();
    final UniqueId fraUid = storeSecurity(securityMaster, fra);
    final UniqueId htsUid = storeHts(htsMaster, iborTs, underlyingId);
    final UniqueId conventionUid = storeConvention(conventionMaster, convention);
    final IntegrationToolContext toolContext = setupToolContext(securityMaster, htsMaster, conventionMaster);
    final CurveUnderlyingsLoader loader = new CurveUnderlyingsLoader();
    loader.setToolContext(toolContext);
    loader.populateDatabases(fra);

  }

  private static UniqueId storeSecurity(final SecurityMaster securityMaster, final ManageableSecurity security) {
    return securityMaster.add(new SecurityDocument(security)).getUniqueId();
  }

  private static UniqueId storeHts(final HistoricalTimeSeriesMaster htsMaster, final LocalDateDoubleTimeSeries expectedTs, final ExternalId id) {
    final ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
    info.setDataField(DATA_FIELD);
    final ExternalIdBundleWithDates bundleWithDates = ExternalIdBundleWithDates.of(id.toBundle());
    info.setExternalIdBundle(bundleWithDates);
    info.setTimeSeriesObjectId(ObjectId.of(id.getScheme().getName(), id.getValue()));
    return htsMaster.updateTimeSeriesDataPoints(info.getTimeSeriesObjectId(), expectedTs);
  }

  private static UniqueId storeConvention(final ConventionMaster conventionMaster, final ManageableConvention convention) {
    return conventionMaster.add(new ConventionDocument(convention)).getUniqueId();
  }

  private IntegrationToolContext setupToolContext(final SecurityMaster securityMaster, final HistoricalTimeSeriesMaster htsMaster,
      final ConventionMaster conventionMaster) {
    final IntegrationToolContext toolContext = new IntegrationToolContext();
    toolContext.setBloombergReferenceDataProvider(_referenceDataProvider);
    toolContext.setSecurityMaster(securityMaster);
    toolContext.setHistoricalTimeSeriesMaster(htsMaster);
    toolContext.setConventionMaster(conventionMaster);
    return toolContext;
  }

  @Test
  public void testUnderlyingsForForwardRateAgreement() {

  }

  @Test
  public void testUnderlyingsForTrs() {

  }

  @Test
  public void testUnderlyingsForStirFuture() {

  }

  @Test
  public void testUnderlyingsForFedFundsFuture() {

  }

  @Test
  public void testUnderlyingsForCapFloor() {

  }

  @Test
  public void testUnderlyingsForCapFloorCmsSpread() {

  }

  private static class MyReferenceDataProvider extends AbstractReferenceDataProvider {

    private final Set<String> _expectedFields = Sets.newHashSet();
    private final Map<String, Multimap<String, String>> _mockDataMap = Maps.newHashMap();

    @Override
    protected ReferenceDataProviderGetResult doBulkGet(final ReferenceDataProviderGetRequest request) {
      if (_expectedFields.size() > 0) {
        for (final String field : _expectedFields) {
          assertTrue(request.getFields().contains(field));
        }
      }
      final ReferenceDataProviderGetResult result = new ReferenceDataProviderGetResult();
      for (final String identifier : request.getIdentifiers()) {
        if (_mockDataMap.containsKey(identifier)) {
          // known security
          final ReferenceData refData = new ReferenceData(identifier);
          final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();

          final Multimap<String, String> fieldMap = _mockDataMap.get(identifier);
          if (fieldMap != null) {
            // security actually has data
            for (final String field : request.getFields()) {
              final Collection<String> values = fieldMap.get(field);
              assertTrue("Field not found: " + field + " in " + fieldMap.keySet(), values.size() > 0);
              assertNotNull(values);
              for (final String value : values) {
                if (value != null) {
                  if (value.contains("=")) {
                    final MutableFudgeMsg submsg = OpenGammaFudgeContext.getInstance().newMessage();
                    submsg.add(StringUtils.substringBefore(value, "="), StringUtils.substringAfter(value, "="));
                    msg.add(field, submsg);
                  } else {
                    msg.add(field, value);
                  }
                }
              }
            }
          }
          refData.setFieldValues(msg);
          result.addReferenceData(refData);

        } else {
          // security wasn't marked as known
          fail("Security not found: " + identifier + " in " + _mockDataMap.keySet());
        }
      }
      return result;
    }

    public void addExpectedField(final String field) {
      _expectedFields.add(field);
    }

    public void addResult(final String securityKey, final String field, final String value) {
      if (field == null) {
        // security is known and normal (empty) result returned
        _mockDataMap.put(securityKey, null);
      } else {
        // security is known and normal data is stored
        Multimap<String, String> baseMap = _mockDataMap.get(securityKey);
        if (baseMap == null) {
          baseMap = ArrayListMultimap.create();
          _mockDataMap.put(securityKey, baseMap);
        }
        baseMap.put(field, value);
      }
    }
  }
}
