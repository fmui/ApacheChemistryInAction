//<start id="asp-listing"/>
<%@ Assembly //<co id="gen-asp-blck" />
    Name="$SharePoint.Project.AssemblyFullName$" %>
<%@ Assembly Name="Microsoft.Web.CommandUI, Version=15.0.0.0,
    Culture=neutral, PublicKeyToken=71e9bce111e9429c" %> 
<%@ Register Tagprefix="SharePoint"
    Namespace="Microsoft.SharePoint.WebControls"
    Assembly="Microsoft.SharePoint,
    Version=15.0.0.0,
    Culture=neutral,
    PublicKeyToken=71e9bce111e9429c" %> 
<%@ Register Tagprefix="Utilities"
    Namespace="Microsoft.SharePoint.Utilities"
    Assembly="Microsoft.SharePoint,
    Version=15.0.0.0,
    Culture=neutral,
    PublicKeyToken=71e9bce111e9429c" %>
<%@ Register Tagprefix="asp"
    Namespace="System.Web.UI"
    Assembly="System.Web.Extensions,
    Version=3.5.0.0,
    Culture=neutral,
    PublicKeyToken=31bf3856ad364e35" %>
<%@ Import Namespace="Microsoft.SharePoint" %> 
<%@ Register Tagprefix="WebPartPages"
    Namespace="Microsoft.SharePoint.WebPartPages"
    Assembly="Microsoft.SharePoint,
    Version=15.0.0.0,
    Culture=neutral,
    PublicKeyToken=71e9bce111e9429c" %>
<%@ Control Language="C#" AutoEventWireup="true"
    CodeBehind="VisualWebPart1.ascx.cs"
    Inherits="CMIS_Web_Part_Project.VisualWebPart1.VisualWebPart1" %>

<asp:UpdatePanel //<co id="postbacks-limited" />
    id="CMISBrowser_UpdatePanel"
    UpdateMode="Conditional"
    ChildrenAsTriggers="true"
    runat="server">
  <ContentTemplate>
    <div id="CMISBrowser_Files_Area"> //<co id="placeholder-list-of-files" />
        <asp:Placeholder 
            id="CMISBrowser_Files" runat="server" />
    </div>
    <asp:HiddenField //<co id="hidden-field-saves-id" />
        id="CMISBrowser_Id"
        runat="server"
        value=""/>
  </ContentTemplate>
  <Triggers>
  </Triggers>
</asp:UpdatePanel>
//<end id="asp-listing"/>