package ekoshkin.teamcity.clouds.kubernetes.web;

import ekoshkin.teamcity.clouds.kubernetes.KubeConstants;
import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnectionCheckResult;
import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnectionSettings;
import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnector;
import ekoshkin.teamcity.clouds.kubernetes.connector.KubeApiConnectorImpl;
import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.BaseFormXmlController;
import jetbrains.buildServer.controllers.BasePropertiesBean;
import jetbrains.buildServer.controllers.admin.projects.PluginPropertiesUtil;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 28.05.17.
 */
public class KubeProfileEditController extends BaseFormXmlController {

    public static final String EDIT_KUBE_HTML = "editKube.html";
    private final String myPath;

    private KubeApiConnector myApiConnector = new KubeApiConnectorImpl();
    private PluginDescriptor myPluginDescriptor;

    public KubeProfileEditController(@NotNull final SBuildServer server,
                                     @NotNull final WebControllerManager web,
                                     @NotNull final PluginDescriptor pluginDescriptor) {
        super(server);
        myPluginDescriptor = pluginDescriptor;
        myPath = pluginDescriptor.getPluginResourcesPath(EDIT_KUBE_HTML);
        web.registerController(myPath, this);
    }

    @Override
    protected ModelAndView doGet(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) {
        ModelAndView modelAndView = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("editProfile.jsp"));
        modelAndView.getModel().put("testConnectionUrl", myPath + "?testConnection=true");
        return modelAndView;
    }

    @Override
    protected void doPost(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Element xmlResponse) {
        BasePropertiesBean propsBean =  new BasePropertiesBean(null);
        PluginPropertiesUtil.bindPropertiesFromRequest(request, propsBean, true);
        final Map<String, String> props = propsBean.getProperties();
        final String apiServerUrl = props.get(KubeConstants.API_SERVER_URL);
        final String accountName = props.get(KubeConstants.SERVICE_ACCOUNT_NAME);
        final String accountToken = props.get(KubeConstants.SERVICE_ACCOUNT_TOKEN);

        if(Boolean.parseBoolean(request.getParameter("testConnection"))){
            KubeApiConnectionCheckResult connectionCheckResult = myApiConnector.testConnection(new KubeApiConnectionSettings(apiServerUrl, accountName, accountToken));
            if(!connectionCheckResult.isSuccess()){
                final ActionErrors errors = new ActionErrors();
                errors.addError("connection-error", connectionCheckResult.getMessage());
                writeErrors(xmlResponse, errors);
            }
        }
    }
}
