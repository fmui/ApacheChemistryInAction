//<start id="dotcmis-webpart" />
using DotCMIS;
using DotCMIS.Binding;
using DotCMIS.Client;
using DotCMIS.Client.Impl;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Web;
using System.Web.UI.WebControls;
using System.Web.UI.WebControls.WebParts;

namespace CMIS_Web_Part_Project.CMISBrowserWebPart
{
    [ToolboxItemAttribute(false)]
    public partial class CMISBrowserWebPart: WebPart
    {
        protected ISession session;

        protected override void OnInit(EventArgs e)
        {
            base.OnInit(e);
            InitializeControl();
            
            //instantiate a session
        }

        protected override void OnLoad(EventArgs e)
        {
            base.OnLoad(e);
            
            EnumerateFiles();
        }

        protected void CMISBrowser_File_Click(object sender,//<co id="click-event-handler" />
             EventArgs e)
        {
            LinkButton link = (LinkButton)sender;

            CMISBrowser_Id.Value = link.Attributes["cmisObjectId"];
            EnumerateFiles();
        }

        protected void Page_Load(object sender, EventArgs e)
        {
        }
    }
}
//<end id="dotcmis-webpart" />