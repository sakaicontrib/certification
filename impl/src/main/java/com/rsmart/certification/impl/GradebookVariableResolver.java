package com.rsmart.certification.impl;

import com.rsmart.certification.api.CertificateDefinition;
import com.rsmart.certification.api.VariableResolutionException;
import com.rsmart.certification.api.criteria.Criterion;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.GradebookItemCriterionHibernateImpl;
import com.rsmart.certification.impl.hibernate.criteria.gradebook.WillExpireCriterionHibernateImpl;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradeDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;

public class GradebookVariableResolver extends AbstractVariableResolver
{
    private static final Log LOG = LogFactory.getLog(GradebookVariableResolver.class);

    private GradebookService gradebookService = null;
    private UserDirectoryService userDirectoryService = null;
    private ToolManager toolManager = null;
    private SecurityService securityService = null;
    private SessionManager sessionManager = null;

    public final String CERT_EXPIREDATE = "cert.expiredate";
    public final String CERT_AWARDDATE  = "cert.date";

    private static final String MESSAGE_EXPIRATION = "variable.expiration";
    private static final String MESSAGE_ISSUEDATE = "variable.issuedate";

    private static final String PERM_VIEWOWNGRADES = "gradebook.viewOwnGrades";
    private static final String PERM_EDITASSIGNMENT = "gradebook.editAssignments";

    public GradebookVariableResolver()
    {
        String expirationDate = getMessages().getString(MESSAGE_EXPIRATION);
        String awardDate = getMessages().getString(MESSAGE_ISSUEDATE);
        addVariable(CERT_EXPIREDATE, expirationDate);
        addVariable(CERT_AWARDDATE, awardDate);
    }

    public String getValue(CertificateDefinition certDef, String varLabel, String userId) throws VariableResolutionException
    {
        if (CERT_EXPIREDATE.equals(varLabel))
        {
            Date issueDate = certDef.getIssueDate(userId);
            if (issueDate == null)
            {
                //shouldn't happen unless new criteria are added where issue date is incalculable
                return "";
            }
            Set<Criterion> awardCriteria = certDef.getAwardCriteria();

            Iterator<Criterion> itAwardCriteria = awardCriteria.iterator();
            while (itAwardCriteria.hasNext())
            {
                Criterion crit = itAwardCriteria.next();
                if (crit instanceof WillExpireCriterionHibernateImpl)
                {
                    //get the offset
                    WillExpireCriterionHibernateImpl wechi = (WillExpireCriterionHibernateImpl) crit;
                    int expiryOffset = Integer.parseInt(wechi.getExpiryOffset());

                    //add the offset to the issue date
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(issueDate);
                    cal.add(Calendar.MONTH, expiryOffset);

                    Date expiryDate = cal.getTime();

                    //return
                    DateFormat sdf = SimpleDateFormat.getDateInstance();
                    return sdf.format(expiryDate);
                }
            }

            return "";
        }
        else if (CERT_AWARDDATE.equals(varLabel))
        {
            Date issueDate = certDef.getIssueDate(userId);
            if (issueDate == null)
            {
                return "";
            }

            DateFormat sdf = SimpleDateFormat.getDateInstance();
            return sdf.format(issueDate);
        }

        throw new VariableResolutionException("could not resolve variable: \"" + varLabel + "\"");
    }

    private Date getDateRecorded(GradebookItemCriterionHibernateImpl criterionImpl) throws VariableResolutionException, AssessmentNotFoundException
    {
        Long gradebookItemId = criterionImpl.getItemId();

        GradeDefinition gradeDef = getGradebookService().getGradeDefinitionForStudentForItem(contextId(), gradebookItemId, userId());
        Date dateRecorded = gradeDef.getDateRecorded();

        if (dateRecorded == null)
        {
            throw new VariableResolutionException("error retrieving date of grade entry");
        }

        return dateRecorded;
    }

    public GradebookService getGradebookService()
    {
        return gradebookService;
    }

    public void setGradebookService(GradebookService gradebookService)
    {
        this.gradebookService = gradebookService;
    }

    public UserDirectoryService getUserDirectoryService()
    {
        return userDirectoryService;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService)
    {
        this.userDirectoryService = userDirectoryService;
    }

    public ToolManager getToolManager()
    {
        return toolManager;
    }

    public void setToolManager (ToolManager toolManager)
    {
        this.toolManager = toolManager;
    }

    public SessionManager getSessionManager()
    {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager)
    {
        this.sessionManager = sessionManager;
    }

    public SecurityService getSecurityService()
    {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService)
    {
        this.securityService = securityService;
    }

    protected final String contextId()
    {
        return getToolManager().getCurrentPlacement().getContext();
    }

    protected final String userId()
    {
        return getUserDirectoryService().getCurrentUser().getId();
    }

    protected Object doSecureGradebookAction(SecureGradebookActionCallback callback) throws Exception
    {
        final SecurityService securityService = getSecurityService();
        final String contextId = contextId();

        try
        {
            securityService.pushAdvisor(new SecurityAdvisor ()
            {
                public SecurityAdvice isAllowed(String userId, String function, String reference)
                {
                    String compTo;
                    if (contextId.startsWith("/site/"))
                    {
                        compTo = contextId;
                    }
                    else
                    {
                        compTo = "/site/" + contextId;
                    }

                    if (reference.equals(compTo) && (PERM_VIEWOWNGRADES.equals(function) ||
                                                     PERM_EDITASSIGNMENT.equals(function)))
                    {
                        return SecurityAdvice.ALLOWED;
                    }
                    else
                    {
                        return SecurityAdvice.PASS;
                    }
                }
            });

            return callback.doSecureAction();
        }
        finally
        {
            securityService.popAdvisor();
        }
    }
}
