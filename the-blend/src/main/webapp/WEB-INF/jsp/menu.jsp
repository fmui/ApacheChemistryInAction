<%-- 
   Copyright 2012 Manning Publications Co.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
--%>
<div class="menu">
<a href="dashboard" class="menulink">Dashboard</a>
<a href="browse" class="menulink">Browse</a>
<a href="search" class="menulink">Search</a>
<a href="tags" class="menulink">Tags</a>
<a href="add" class="menulink">Add</a>
<a href="index?logout=true" class="menulink">Logout</a>

<div style="float: right; margin-right: 10px;">
<form method="POST" action="search">
<input type="text" name="q"> <input type="submit" value="go">
</form>
</div>
</div>