/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.servlet.QueryBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.*;
import java.net.URL;


/**
 * Utility class for getting / setting global properties.
 */
public class GlobalProperties {

    public static final String ENV_PREFIX = "PORTAL_";

    public static final String HOME_DIR = "PORTAL_HOME";
    private static final String propertiesFilename = "portal.properties";

    public static final String PATHWAY_COMMONS_URL = "pathway_commons.url";
    public static final String UCSC_CANCER_GENOMICS_URL = "ucsc_cancer_genomics.url";
	public static final String SEGFILE_URL = "segfile.url";
	public static final String OPENSSL_BINARY = "openssl.binary";
	public static final String SIGNATURE_KEY = "signature.key";
	public static final String ENCRYPTION_KEY = "encryption.key";
	public static final String BROAD_BAM_URL = "broad.bam.url";
	public static final String BROAD_BAM_CHECKING_URL = "broad.bam.checking.url";
	public static final String IGV_BAM_LINKING = "igv.bam.linking";
	public static final String IGV_BAM_LINKING_STUDIES = "igv.bam.linking.studies";
    public static final String AUTHENTICATE = "authenticate";
    public static final String AUTHORIZATION = "authorization";
    public static final String FILTER_GROUPS_BY_APPNAME = "filter_groups_by_appname";
    public static final String INCLUDE_NETWORKS = "include_networks";
    public static final String GOOGLE_ANALYTICS_PROFILE_ID = "google_analytics_profile_id";
    public static final String GENOMESPACE = "genomespace";

    public static final String APP_NAME = "app.name";
    public static final String DEFAULT_APP_NAME = "public_portal";
    
    public static final String APP_VERSION = "app.version";
    public static final String SKIN_TITLE = "skin.title";
    public static final String DEFAULT_SKIN_TITLE = "cBioPortal for Cancer Genomics";
    public static final String SKIN_BLURB = "skin.blurb";
    // blurb default
    public static final String DEFAULT_SKIN_BLURB = "The cBioPortal for Cancer Genomics provides " +
            "<b>visualization</b>, <b>analysis</b> and <b>download</b> of large-scale cancer genomics data sets. " +
            "<p>Please adhere to <u><a href=\"http://cancergenome.nih.gov/abouttcga/policies/publicationguidelines\"> " +
            "the TCGA publication guidelines</a></u> when using TCGA data in your publications.</p> " +
            "<p><b>Please cite</b> <a href=\"http://www.ncbi.nlm.nih.gov/pubmed/23550210\">Gao " +
            "et al. <i>Sci. Signal.</i> 2013</a> &amp;  " +
            "<a href=\"http://cancerdiscovery.aacrjournals.org/content/2/5/401.abstract\">" +
            "Cerami et al. <i>Cancer Discov.</i> 2012</a> when publishing results based on cBioPortal.</p>\n";
    public static final String SKIN_TAG_LINE_IMAGE = "skin.tag_line_image";
    public static final String DEFAULT_SKIN_TAG_LINE_IMAGE = "images/tag_line.png";
    public static final String SKIN_EMAIL_CONTACT = "skin.email_contact";
    public static final String DEFAULT_EMAIL_CONTACT = "cbioportal at googlegroups dot com";
    public static final String SKIN_SHOW_NEWS_TAB = "skin.show_news_tab";
    public static final String SKIN_SHOW_DATA_TAB = "skin.show_data_tab";
    public static final String SKIN_RIGHT_NAV_SHOW_DATA_SETS = "skin.right_nav.show_data_sets";
    public static final String SKIN_RIGHT_NAV_SHOW_EXAMPLES = "skin.right_nav.show_examples";
    public static final String SKIN_RIGHT_NAV_SHOW_TESTIMONIALS = "skin.right_nav.show_testimonials";
    public static final String SKIN_AUTHORIZATION_MESSAGE = "skin.authorization_message";
    public static final String DEFAULT_AUTHORIZATION_MESSAGE = "Access to this portal is only available to authorized users.";
    public static final String SKIN_EXAMPLE_STUDY_QUERIES = "skin.example_study_queries";
    public static final String DEFAULT_SKIN_EXAMPLE_STUDY_QUERIES =
            "tcga\n" +
            "tcga -provisional\n" +
            "tcga -moratorium\n" +
            "tcga OR icgc\n" +
            "-\"cell line\"\n" +
            "prostate mskcc\n" +
            "esophageal OR stomach\n" +
            "serous\n" +
            "breast";
    public static final String SKIN_DATASETS_HEADER = "skin.data_sets_header";
    public static final String DEFAULT_SKIN_DATASETS_HEADER = "The portal currently contains data from the following " +
            "cancer genomics studies.  The table below lists the number of available samples per data type and tumor.";
    public static final String SKIN_DATASETS_FOOTER = "skin.data_sets_footer";
    public static final String DEFAULT_SKIN_DATASETS_FOOTER = "Data sets of TCGA studies were downloaded from Broad " +
            "Firehose (http://gdac.broadinstitute.org) and updated monthly. In some studies, data sets were from the " +
            "TCGA working groups directly.";

    public static final String PATIENT_VIEW_PLACEHOLDER = "patient_view_placeholder";
    public static final String PATIENT_VIEW_GENOMIC_OVERVIEW_CNA_CUTOFF = "patient_view_genomic_overview_cna_cutoff";
    public static final double[] DEFAULT_GENOMIC_OVERVIEW_CNA_CUTOFF = new double[]{0.2,1.5};
    public static final String PATIENT_VIEW_DIGITAL_SLIDE_IFRAME_URL = "digitalslidearchive.iframe.url";
    public static final String PATIENT_VIEW_DIGITAL_SLIDE_META_URL = "digitalslidearchive.meta.url";
    public static final String PATIENT_VIEW_TCGA_PATH_REPORT_URL = "tcga_path_report.url";

    public static final String PATIENT_VIEW_MDACC_HEATMAP_META_URL = "mdacc.heatmap.meta.url";
    public static final String PATIENT_VIEW_MDACC_HEATMAP_URL = "mdacc.heatmap.patient.url";

    public static final String STUDY_VIEW_MDACC_HEATMAP_URL = "mdacc.heatmap.study.url";
    public static final String STUDY_VIEW_MDACC_HEATMAP_META_URL = "mdacc.heatmap.study.meta.url";

    public static final String ONCOKB_API_URL = "oncokb.api.url";
    public static final String SHOW_ONCOKB = "show.oncokb";

    public static final String SESSION_SERVICE_URL = "session.service.url";

    // properties for showing the right logo in the header_bar and default logo
    public static final String SKIN_RIGHT_LOGO = "skin.right_logo";
    public static final String DEFAULT_SKIN_RIGHT_LOGO = "images/mskcc_logo_3d_grey.jpg";

    // properties for hiding/showing tabs in the header navigation bar
    public static final String SKIN_SHOW_WEB_API_TAB = "skin.show_web_api_tab";
    public static final String SKIN_SHOW_R_MATLAB_TAB = "skin.show_r_matlab_tab";
    public static final String SKIN_SHOW_TUTORIALS_TAB = "skin.show_tutorials_tab";
    public static final String SKIN_SHOW_FAQS_TAB = "skin.show_faqs_tab";
    public static final String SKIN_SHOW_TOOLS_TAB = "skin.show_tools_tab";
    public static final String SKIN_SHOW_ABOUT_TAB = "skin.show_about_tab";
    public static final String SKIN_SHOW_VISUALIZE_YOUR_DATA_TAB = "skin.show_visualize_your_data_tab";
    public static final String SKIN_PATIENT_VIEW_SHOW_CLINICAL_TRIALS_TAB = "skin.show_clinical_trials_tab";

    // property for setting the news blurb in the right column
    public static final String SKIN_RIGHT_NAV_WHATS_NEW_BLURB = "skin.right_nav.whats_new_blurb";
    public static final String DEFAULT_SKIN_WHATS_NEW_BLURB = 
            "<form action=\"http://groups.google.com/group/cbioportal-news/boxsubscribe\"> &nbsp;&nbsp;&nbsp;&nbsp;" +
            "<b>Sign up for low-volume email news alerts:</b></br> &nbsp;&nbsp;&nbsp;&nbsp;<input type=\"text\" " +
            "name=\"email\" title=\"Subscribe to mailing list\"> <input type=\"submit\" name=\"sub\" value=\"Subscribe\"> " +
            "</form> &nbsp;&nbsp;&nbsp;&nbsp;<b>Or follow us <a href=\"http://www.twitter.com/cbioportal\">" +
            "<i>@cbioportal</i></a> on Twitter</b>\n";

    // footer
    public static final String SKIN_FOOTER = "skin.footer";
    public static final String DEFAULT_SKIN_FOOTER = " | <a href=\"http://www.mskcc.org/mskcc/html/44.cfm\">MSKCC</a>" +
            " | <a href=\"http://cancergenome.nih.gov/\">TCGA</a>";

    // login contact
    public static final String SKIN_LOGIN_CONTACT_HTML = "skin.login.contact_html";
    public static final String DEFAULT_SKIN_LOGIN_CONTACT_HTML = "If you think you have received this message in " +
            "error, please contact us at <a style=\"color:#FF0000\" href=\"mailto:cbioportal-access@cbio.mskcc.org\">" +
            "cbioportal-access@cbio.mskcc.org</a>";

    // properties for hiding/showing tabs in the patient view
    public static final String SKIN_PATIENT_VIEW_SHOW_DRUGS_TAB="skin.patient_view.show_drugs_tab";

    // property for setting the saml registration html
    public static final String SKIN_LOGIN_SAML_REGISTRATION_HTML = "skin.login.saml.registration_html";
    public static final String DEFAULT_SKIN_LOGIN_SAML_REGISTRATION_HTML = "Sign in with MSK";

    // property for the saml entityid
    public static final String SAML_IDP_METADATA_ENTITYID="saml.idp.metadata.entityid";

    // property for the custom header tabs
    public static final String SKIN_CUSTOM_HEADER_TABS="skin.custom_header_tabs";

    // properties for the FAQ, about us, news and examples
    public static final String SKIN_BASEURL="skin.documentation.baseurl";
    public static final String DEFAULT_SKIN_BASEURL="https://raw.githubusercontent.com/cBioPortal/cbioportal/master/docs/";
    public static final String SKIN_DOCUMENTATION_MARKDOWN="skin.documentation.markdown";

    public static final String SKIN_FAQ="skin.documentation.faq";
    public static final String DEFAULT_SKIN_FAQ="FAQ.md";
    public static final String SKIN_ABOUT="skin.documentation.about";
    public static final String DEFAULT_SKIN_ABOUT="About-Us.md";
    public static final String SKIN_NEWS="skin.documentation.news";
    public static final String DEFAULT_SKIN_NEWS="News.md";

    public static final String SKIN_EXAMPLES_RIGHT_COLUMN="skin.examples_right_column";
    public static final String DEFAULT_SKIN_EXAMPLES_RIGHT_COLUMN="../../../content/examples.html";
    
    public static final String ALWAYS_SHOW_STUDY_GROUP="always_show_study_group";

    // property for text shown at the right side of the Select Patient/Case set, which
    // links to the study view
    public static final String SKIN_STUDY_VIEW_LINK_TEXT="skin.study_view.link_text";
    public static final String DEFAULT_SKIN_STUDY_VIEW_LINK_TEXT="To build your own case set, try out our enhanced " +
            "Study View.";

    public static final String MYCANCERGENOME_SHOW = "mycancergenome.show";
    public static final String ONCOKB_GENE_STATUS = "oncokb.geneStatus";
    public static final String SHOW_HOTSPOT = "show.hotspot";
    
    public static final String RECACHE_STUDY_AFTER_UPDATE = "recache_study_after_update";
    
    public static final String DB_VERSION = "db.version";
    
    public static final String DARWIN_AUTH_URL = "darwin.auth_url";
    public static final String DARWIN_RESPONSE_URL = "darwin.response_url";
    public static final String DARWIN_AUTHORITY = "darwin.authority";
    public static final String CIS_USER = "cis.user";
    public static final String DISABLED_TABS = "disabled_tabs";
    
    public static final String PRIORITY_STUDIES = "priority_studies";
    
    private static Log LOG = LogFactory.getLog(GlobalProperties.class);
    private static Properties properties = initializeProperties();

    private static Properties initializeProperties()
    {
        return loadProperties(getResourceStream());
    }

    private static InputStream getResourceStream()
    {
        String resourceFilename = null;
        InputStream resourceFIS = null;

        try {
            String home = System.getenv(HOME_DIR);
            if (home != null) {
                 resourceFilename =
                    home + File.separator + GlobalProperties.propertiesFilename;
                if (LOG.isInfoEnabled()) {
                    LOG.info("Attempting to read properties file: " + resourceFilename);
                }
                resourceFIS = new FileInputStream(resourceFilename);
                if (LOG.isInfoEnabled()) {
                    LOG.info("Successfully read properties file");
                }
            }
        }
        catch (FileNotFoundException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Failed to read properties file: " + resourceFilename);
            }
        }

        if (resourceFIS == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Attempting to read properties file from classpath");
            }
            resourceFIS = GlobalProperties.class.getClassLoader().
                getResourceAsStream(GlobalProperties.propertiesFilename);
            if (LOG.isInfoEnabled()) {
                LOG.info("Successfully read properties file");
            }
        }
         
        return resourceFIS;
    }

    private static Properties loadProperties(InputStream resourceInputStream)
    {
        Properties properties = new Properties();

        try {
            properties.load(resourceInputStream);
            resourceInputStream.close();
        }
        catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error loading properties file: " + e.getMessage());
            }
        }

        return properties;
    }

    public static String getPathwayCommonsUrl()
	{
		return getProperty(PATHWAY_COMMONS_URL);
    }
    
    public static String getUcscCancerGenomicsUrl()
	{
        return getProperty(UCSC_CANCER_GENOMICS_URL);
    }

    public static String getSegfileUrl()
	{
        return getProperty(SEGFILE_URL);
    }

    /**
     * Return property with the given name from environment variables or from properties file.
     *
     * First, the function attempts to read the variable from System.getenv() using the
     * prefix ENV_PREFIX and replacing characters not matching [a-zA-Z_] with "__".
     * If this is not set (returns null or is empty), then the value from the properties file
     * is used.
     * 
     * @param property Name of the property to query for
     * @return Value of the property, or <code>null</code> if not found.
     */
	public static String getProperty(String property)
	{
	    String res = System.getenv(ENV_PREFIX + property.replaceAll("[^a-zA-Z_]",  "__"));
	    if (res != null && !res.isEmpty())
	        return res;
		return (properties.containsKey(property)) ? properties.getProperty(property) : null;
	}

	public static boolean wantIGVBAMLinking() {
        String igvBamLinking = getProperty(IGV_BAM_LINKING);
		return igvBamLinking!=null && igvBamLinking.equals("true");
	}

	public static Collection<String> getIGVBAMLinkingStudies() {
        String igvBamLinkingStudies = getProperty(IGV_BAM_LINKING_STUDIES);
        if (igvBamLinkingStudies==null) {
            return Collections.emptyList();
        }
		String[] studies = igvBamLinkingStudies.split(":");
		return (studies.length > 0) ? Arrays.asList(studies) : Collections.<String>emptyList();
	}

    public static boolean usersMustAuthenticate()
    {
        String prop = getProperty(AUTHENTICATE);
        return (!prop.isEmpty() && !prop.equals("false"));
    }

    public static String authenticationMethod()
    {
        return getProperty(AUTHENTICATE);
    }

    /**
     * Return authenticated username
     * @return String userName 
     * Return authenticated username. If the user is not authenticated, 'anonymousUser' will be returned.
     */
    public static String getAuthenticatedUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if(authentication != null) {
            return authentication.getName();
        }else {
            return "anonymousUser";
        }
    }
	public static boolean usersMustBeAuthorized() {
        return Boolean.parseBoolean(getProperty(AUTHORIZATION));
	}

    public static String getAppName()
    {
        String appName = getProperty(APP_NAME);
        return (appName == null) ? DEFAULT_APP_NAME : appName;
    }

    public static String getAppVersion()
    {
        String appVersion = getProperty(APP_VERSION);
        return (appVersion == null) ? "1.0" : appVersion;
    }
    
    public static String getTitle()
    {
        String skinTitle = getProperty(SKIN_TITLE);
        return (skinTitle == null) ? DEFAULT_SKIN_TITLE : skinTitle;
    }
    // updated function to use a default if nothing is specified
    public static String getBlurb()
    {
        String skinBlurb = getProperty(SKIN_BLURB);
        return (skinBlurb == null) ? DEFAULT_SKIN_BLURB : skinBlurb;
    }
    // get custom FAQ html or the default
    public static String getFaqHtml()
    {
        String faqHtml = getProperty(SKIN_FAQ);
        return (faqHtml == null) ? DEFAULT_SKIN_FAQ : getContentString(faqHtml);
    }
    // get custom About html or the default
    public static String getAboutHtml()
    {
        String aboutHtml = getProperty(SKIN_ABOUT);
        return (aboutHtml == null) ? DEFAULT_SKIN_ABOUT : getContentString(aboutHtml);
    }
    // get custom News html or the default
    public static String getNewsHtml()
    {
        String newsHtml = getProperty(SKIN_NEWS);
        return (newsHtml == null) ? DEFAULT_SKIN_NEWS : getContentString(newsHtml);
    }
    // get custom News html or the default
    public static String getBaseUrl()
    {
        String baseUrl = getProperty(SKIN_BASEURL);
        return (baseUrl == null) ? DEFAULT_SKIN_BASEURL : baseUrl;
    }
    public static boolean isMarkdownDocumentation()
    {
        String markdownFlag = getProperty(SKIN_DOCUMENTATION_MARKDOWN);
        return markdownFlag == null || Boolean.parseBoolean(markdownFlag);
    }

    // get custom Example Queries for the right column html or the default
    public static String getExamplesRightColumnHtml()
    {
        String examplesRightColumnHtml = getProperty(SKIN_EXAMPLES_RIGHT_COLUMN);
        return (examplesRightColumnHtml == null) ? DEFAULT_SKIN_EXAMPLES_RIGHT_COLUMN : "../../../content/"+examplesRightColumnHtml;
    }

    private static String getContentString(String contentString){
        if(getBaseUrl().equalsIgnoreCase("")) return "content/"+contentString;
        return contentString;
    }

    // get the login contact html
    public static String getLoginContactHtml()
    {
        String loginContactHtml = getProperty(SKIN_LOGIN_CONTACT_HTML);
        return (loginContactHtml == null) ? DEFAULT_SKIN_LOGIN_CONTACT_HTML : loginContactHtml;
    }
    // get the text for the saml login button
    public static String getLoginSamlRegistrationHtml()
    {
        String loginSamlRegistrationHtml = getProperty(SKIN_LOGIN_SAML_REGISTRATION_HTML);
        return (loginSamlRegistrationHtml == null) ? DEFAULT_SKIN_LOGIN_SAML_REGISTRATION_HTML : loginSamlRegistrationHtml;
    }
    public static String getSamlIdpMetadataEntityid()
    {
        return getProperty(SAML_IDP_METADATA_ENTITYID);
    }
    public static String getTagLineImage()
    {
        String tagLineImage = getProperty(SKIN_TAG_LINE_IMAGE);
        return (tagLineImage == null) ? DEFAULT_SKIN_TAG_LINE_IMAGE : "images/" + tagLineImage;
    }

    // function for retrieving the right logo, used by the header_bar
    public static String getRightLogo()
    {
        String rightLogo = getProperty(SKIN_RIGHT_LOGO);
        return (rightLogo == null) ? DEFAULT_SKIN_RIGHT_LOGO : "images/" + rightLogo;
    }

    // function for retrieving the footer text
    public static String getFooter(){
        String footer = getProperty(SKIN_FOOTER);
        return (footer == null) ? DEFAULT_SKIN_FOOTER : footer;
    }
    // function for retrieving the studyview link text
    public static String getStudyviewLinkText(){
        String studyviewLinkText = getProperty(SKIN_STUDY_VIEW_LINK_TEXT);
        return (studyviewLinkText == null) ? DEFAULT_SKIN_STUDY_VIEW_LINK_TEXT : studyviewLinkText;
    }

    public static String getEmailContact()
    {
        String emailAddress = getProperty(SKIN_EMAIL_CONTACT);
        return (emailAddress == null) ? DEFAULT_EMAIL_CONTACT :
            ("<span class=\"mailme\" title=\"Contact us\">" + emailAddress + "</span>");
    }

    public static boolean includeNetworks()
    {
        return Boolean.parseBoolean(getProperty(INCLUDE_NETWORKS));
    }

    public static String getGoogleAnalyticsProfileId()
    {
        return getProperty(GOOGLE_ANALYTICS_PROFILE_ID);
    }

    public static boolean genomespaceEnabled()
    {
        return Boolean.parseBoolean(getProperty(GENOMESPACE));
    }

    public static boolean showPlaceholderInPatientView()
    {
        return Boolean.parseBoolean(getProperty(PATIENT_VIEW_PLACEHOLDER));
    }

    public static double[] getPatientViewGenomicOverviewCnaCutoff()
    {
        String cutoff = getProperty(PATIENT_VIEW_GENOMIC_OVERVIEW_CNA_CUTOFF);
        if (cutoff==null) {
            return DEFAULT_GENOMIC_OVERVIEW_CNA_CUTOFF;
        }

        String[] strs = cutoff.split(",");
        return new double[]{Double.parseDouble(strs[0]), Double.parseDouble(strs[1])};
    }

    public static boolean showNewsTab()
    {
        String showFlag = getProperty(SKIN_SHOW_NEWS_TAB);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }

    public static boolean showDataTab()
    {
        String showFlag = getProperty(SKIN_SHOW_DATA_TAB);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }

    // show or hide the web api tab in header navigation bar
    public static boolean showWebApiTab()
    {
        String showFlag = getProperty(SKIN_SHOW_WEB_API_TAB);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }
    // show or hide the r matlab tab in header navigation bar
    public static boolean showRMatlabTab()
    {
        String showFlag = getProperty(SKIN_SHOW_R_MATLAB_TAB);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }
    // show or hide the tutorial tab in header navigation bar
    public static boolean showTutorialsTab()
    {
        String showFlag = getProperty(SKIN_SHOW_TUTORIALS_TAB);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }
    // show or hide the faqs tab in header navigation bar
    public static boolean showFaqsTab()
    {
        String showFlag = getProperty(SKIN_SHOW_FAQS_TAB);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }
    // show or hide the tools tab in header navigation bar
    public static boolean showToolsTab()
    {
        String showFlag = getProperty(SKIN_SHOW_TOOLS_TAB);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }
    // show or hide the about tab in header navigation bar
    public static boolean showAboutTab()
    {
        String showFlag = getProperty(SKIN_SHOW_ABOUT_TAB);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }
    // show or hide the visualize your data tab in header navigation bar
    public static boolean showVisualizeYourDataTab()
    {
        String showFlag = getProperty(SKIN_SHOW_VISUALIZE_YOUR_DATA_TAB);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }

    // show the clinical trials tab in the patient view
    public static boolean showClinicalTrialsTab()
    {
        String showFlag = getProperty(SKIN_PATIENT_VIEW_SHOW_CLINICAL_TRIALS_TAB);
        return showFlag != null && Boolean.parseBoolean(showFlag);
    }

    // show the drugs tab in the patient view
    public static boolean showDrugsTab()
    {
        String showFlag = getProperty(SKIN_PATIENT_VIEW_SHOW_DRUGS_TAB);
        return showFlag != null && Boolean.parseBoolean(showFlag);
    }
    // get the text for the What's New in the right navigation bar
    public static String getRightNavWhatsNewBlurb(){
        String whatsNewBlurb = getProperty(SKIN_RIGHT_NAV_WHATS_NEW_BLURB);
        return (whatsNewBlurb == null) ? DEFAULT_SKIN_WHATS_NEW_BLURB : whatsNewBlurb;
    }
    public static boolean showRightNavDataSets()
    {
        String showFlag = getProperty(SKIN_RIGHT_NAV_SHOW_DATA_SETS);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }
    public static boolean showRightNavExamples()
    {
        String showFlag = getProperty(SKIN_RIGHT_NAV_SHOW_EXAMPLES);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }

    public static boolean showRightNavTestimonials()
    {
        String showFlag = getProperty(SKIN_RIGHT_NAV_SHOW_TESTIMONIALS);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }

    public static String getAuthorizationMessage()
    {
        String authMessage = getProperty(SKIN_AUTHORIZATION_MESSAGE);
        return authMessage == null ? DEFAULT_AUTHORIZATION_MESSAGE : authMessage;
    }

    public static String getExampleStudyQueries() {
        String result = getProperty(SKIN_EXAMPLE_STUDY_QUERIES);
        if (result == null)
            return DEFAULT_SKIN_EXAMPLE_STUDY_QUERIES;
        else
            return result;
    }

    // added usage of default data sets header
    public static String getDataSetsHeader()
    {
        String dataSetsHeader = getProperty(SKIN_DATASETS_HEADER);
        return dataSetsHeader == null ? DEFAULT_SKIN_DATASETS_HEADER : dataSetsHeader;
    }

    // added usage of default data sets footer
    public static String getDataSetsFooter()
    {
        String dataSetsFooter = getProperty(SKIN_DATASETS_FOOTER);
        return dataSetsFooter == null ? DEFAULT_SKIN_DATASETS_FOOTER : dataSetsFooter;
    }

    public static String getLinkToPatientView(String caseId, String cancerStudyId)
    {
        return "case.do?" + QueryBuilder.CANCER_STUDY_ID + "=" + cancerStudyId
                 //+ "&"+ org.mskcc.cbio.portal.servlet.PatientView.PATIENT_ID + "=" + caseId;
                 + "&"+ org.mskcc.cbio.portal.servlet.PatientView.SAMPLE_ID + "=" + caseId;
    }

    public static String getLinkToSampleView(String caseId, String cancerStudyId)
    {
        return "case.do?" + QueryBuilder.CANCER_STUDY_ID + "=" + cancerStudyId
                 + "&"+ org.mskcc.cbio.portal.servlet.PatientView.SAMPLE_ID + "=" + caseId;
    }

    public static String getLinkToCancerStudyView(String cancerStudyId)
    {
        return "study?" + org.mskcc.cbio.portal.servlet.CancerStudyView.ID
                + "=" + cancerStudyId;
    }

    public static String getLinkToIGVForBAM(String cancerStudyId, String caseId, String locus)
    {
        return ("igvlinking.json?" +
				org.mskcc.cbio.portal.servlet.IGVLinkingJSON.CANCER_STUDY_ID +
                "=" + cancerStudyId +
				"&" + org.mskcc.cbio.portal.servlet.IGVLinkingJSON.CASE_ID +
				"=" + caseId +
				"&" + org.mskcc.cbio.portal.servlet.IGVLinkingJSON.LOCUS +
				"=" + locus);
    }

    public static String getDigitalSlideArchiveIframeUrl(String caseId)
    {
        String url = getProperty(PATIENT_VIEW_DIGITAL_SLIDE_IFRAME_URL);
        return url+caseId;
    }

    public static String getDigitalSlideArchiveMetaUrl(String caseId)
    {
        String url = getProperty(PATIENT_VIEW_DIGITAL_SLIDE_META_URL);
        return url+caseId;
    }

    public static String getStudyHeatmapMetaUrl()
    {
        String url = properties.getProperty(STUDY_VIEW_MDACC_HEATMAP_META_URL);
        return url;
    }

    public static String getStudyHeatmapViewerUrl()
    {
        String url = properties.getProperty(STUDY_VIEW_MDACC_HEATMAP_URL);
        return url;
    }

    public static String getPatientHeatmapMetaUrl(String caseId)
    {
        String url = properties.getProperty(PATIENT_VIEW_MDACC_HEATMAP_META_URL);
        if (url == null || url.length() == 0) return null;
        return url + caseId;
    }

    public static String getPatientHeatmapViewerUrl(String caseId)
    {
        String url = properties.getProperty(PATIENT_VIEW_MDACC_HEATMAP_URL);
        if (url == null || url.length() == 0) return null;
        return url + caseId;
    }

    public static String getTCGAPathReportUrl()
    {
        String url = GlobalProperties.getProperty(PATIENT_VIEW_TCGA_PATH_REPORT_URL);
        if (url == null) {
            return null;
        }       
        
        return url;
    }

    // function for getting the custom tabs for the header
    public static String[] getCustomHeaderTabs(){
        String customPagesString = GlobalProperties.getProperty(SKIN_CUSTOM_HEADER_TABS);
        if(customPagesString!=null){
            // split by comma and return the String array
            return customPagesString.split(",");
        }
        return null;
    }
   
    public static String getSessionServiceUrl()
    {
        return getProperty(SESSION_SERVICE_URL);
    }
 
    public static String getOncoKBApiUrl()
    {
        String oncokbApiUrl = properties.getProperty(ONCOKB_API_URL);
        String showOncokb = properties.getProperty(SHOW_ONCOKB);

        if(showOncokb == null || showOncokb.isEmpty()) {
            showOncokb = "true";
        }
        // This only applies if there is no oncokb.api.url property in the portal.properties file.
        // Empty string should be used if you want to disable the OncoKB annotation.
        if(oncokbApiUrl == null) {
            oncokbApiUrl = "http://oncokb.org/legacy-api/";
        }

        //Test connection of OncoKB website.
        if(!oncokbApiUrl.isEmpty() && showOncokb.equals("true")) {
            try {
                URL url = new URL(oncokbApiUrl+"access");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if(conn.getResponseCode() != 200) {
                    oncokbApiUrl = "";
                }
                conn.disconnect();
                return oncokbApiUrl;
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }

    public static boolean showHotspot() {
        String hotspot = getProperty(SHOW_HOTSPOT);
        if (hotspot==null) {
            return true; // show hotspots by default
        }
        
        if(!hotspot.isEmpty()) {
            return Boolean.parseBoolean(hotspot);
        }else{
            return false;
        }
    }

    public static boolean filterGroupsByAppName() {
        String filterGroupsByNameFlag = getProperty(FILTER_GROUPS_BY_APPNAME);
        return filterGroupsByNameFlag == null || Boolean.parseBoolean(filterGroupsByNameFlag);
    }
    
    public static String getAlwaysShowStudyGroup() {
        String group = getProperty(ALWAYS_SHOW_STUDY_GROUP);
        if (group!=null && group.trim().isEmpty()) {
            return null;
        }
        
        return group;
    }
    
    public static boolean showMyCancerGenomeUrl()
    {
        String show = getProperty(MYCANCERGENOME_SHOW);
        return show != null && Boolean.parseBoolean(show);
    }
    
    public static String getOncoKBGeneStatus()
    {
        return getProperty(ONCOKB_GENE_STATUS);
    }
    
    public static boolean getRecacheStudyAfterUpdate() {
        String recacheStudyAfterUpdate = getProperty(RECACHE_STUDY_AFTER_UPDATE);
        if (recacheStudyAfterUpdate==null || recacheStudyAfterUpdate.isEmpty()) {
            return false;
        }
        return Boolean.parseBoolean(recacheStudyAfterUpdate);
    }
    
    public static String getDbVersion() {
        String version = getProperty(DB_VERSION);
        if (version == null)
        {
            return "0";
        }
        return version;
    }
    
    public static String getDarwinAuthCheckUrl() {
        String darwinAuthUrl = "";
        try{
            darwinAuthUrl = getProperty(DARWIN_AUTH_URL).trim();            
        }
        catch (NullPointerException e){}
        
        return darwinAuthUrl;
    }
    
    public static String getDarwinResponseUrl() {
        String darwinResponseUrl = "";
        try{
            darwinResponseUrl = getProperty(DARWIN_RESPONSE_URL).trim();
        }
        catch (NullPointerException e) {}
        
        return darwinResponseUrl;
    }
    
    public static String getDarwinAuthority() { 
        String darwinAuthority = "";
        try{
            darwinAuthority = getProperty(DARWIN_AUTHORITY).trim();
        }
        catch (NullPointerException e) {}
        
        return darwinAuthority;
    }
    
    public static List<String[]> getPriorityStudies() {
	    List<String[]> priorityStudiesObject = new LinkedList<>();
	    try {
		    String priorityStudies = getProperty(PRIORITY_STUDIES).trim();
		    for (String priorityStudyCategory: priorityStudies.split(";")) {
			    String[] elements = priorityStudyCategory.split("[#,]");
			    elements = Arrays.stream(elements).filter(s -> s.length() > 0).toArray(String[]::new);
			    if (elements.length > 1) {
				    priorityStudiesObject.add(elements);
			    }
		    }
	    } catch (NullPointerException e) {}
	    return priorityStudiesObject;
    }
    
    public static String getCisUser() {
        String cisUser = "";
        try{
            cisUser = getProperty(CIS_USER).trim();
        }
        catch (NullPointerException e) {}
        
        return cisUser;         
    }
    
    public static List<String> getDisabledTabs() {
        String disabledTabs = "";
        try {
            disabledTabs = getProperty(DISABLED_TABS).trim();
        }
        catch (NullPointerException e) {}
        
        String[] tabs = disabledTabs.split("\\|");
        return (tabs.length > 0 && disabledTabs.length() > 0) ? Arrays.asList(tabs) : new ArrayList<String>();
    }

    public static void main(String[] args)
    {
        System.out.println(getAppVersion());
    }
}
