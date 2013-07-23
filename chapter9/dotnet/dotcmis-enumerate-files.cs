//<start id="enumerate-files-method" />
        protected void EnumerateFiles()
        {
            CMISBrowser_Files.Controls.Clear();
            ICmisObject cmisObject =
               string.IsNullOrEmpty(CMISBrowser_Id.Value) ?
                 session.GetRootFolder() :
                 session.GetObject(new ObjectId(CMISBrowser_Id.Value));

            if (cmisObject.BaseTypeId ==
                DotCMIS.Enums.BaseTypeId.CmisFolder)
            {
                IFolder cmisFolder = (IFolder)cmisObject;
                if (cmisFolder.FolderParent != null)
                {
                    string folderParentId = cmisFolder.FolderParent.Id;
                    LinkButton upLink = new LinkButton(); //<co id="uplink" />
                    upLink.Click += new EventHandler(CMISBrowser_File_Click);
                    upLink.Text = "Up One Level";
                    upLink.Attributes["cmisObjectId"] = folderParentId;
                    upLink.ID = "cmisObjectId" + folderParentId; //<co id="ID_required" />
                    CMISBrowser_Files.Controls.Add(upLink);
                }
                foreach (ICmisObject cmisChild in cmisFolder.GetChildren())//<co id="folder-children"/>
                {
                    if (cmisChild.BaseTypeId ==
                        DotCMIS.Enums.BaseTypeId.CmisFolder)
                    {
                        IFolder cmisChildFolder = (IFolder)cmisChild;
                        LinkButton childLink = new LinkButton();
                        childLink.Click +=
                             new EventHandler(CMISBrowser_File_Click);
                        childLink.Text =
                             HttpUtility.HtmlEncode(cmisChildFolder.Name);
                        childLink.Attributes["cmisObjectId"] =
                             cmisChildFolder.Id;
                        childLink.ID = "cmisObjectId" + cmisChildFolder.Id; //<co id="ID_required_2" />
                        CMISBrowser_Files.Controls.Add(childLink);
                    }
                    else
                    {
                        Literal childText = new Literal();
                        childText.Text = //<co id="display-name-only"/>
                             "<span class='CMISBrowser_Document'>" +
                             HttpUtility.HtmlEncode(cmisChild.Name) +
                             "</span>";
                        childText.ID = "cmisObjectId" + cmisChild.Id;
                        CMISBrowser_Files.Controls.Add(childText);
                    }
                }
            }
        }
//<end id="enumerate-files-method" />