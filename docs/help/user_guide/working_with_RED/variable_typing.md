<html>
<head>
<link href="PLUGINS_ROOT/org.robotframework.ide.eclipse.main.plugin.doc.user/help/style.css" rel="stylesheet" type="text/css"/>
</head>
<body>
<a href="RED/../../../../help/index.html">RED - Robot Editor User Guide</a> &gt; <a href="RED/../../../../help/user_guide/user_guide.html">User guide</a> &gt; <a href="RED/../../../../help/user_guide/working_with_RED.html">Working with RED</a> &gt; 
<h2>Variable typing in editors</h2>
RED supports several variable typing enhancements. When this feature is enabled, variable brackets {} are automatically inserted after typing one of Robot variable identifiers ($, @, &amp;, %).<br/>
Corresponding bracket is automatically deleted when brackets are empty and first bracket is deleted by typing Delete or Backspace.
When selected text matches given pattern and one of variable identifiers is typed, selection is wrapped with variable brackets.

<br/><br/><img src="images/variable_typing.gif"/> <br/><br/>
<h3>Variable typing preferences</h3>
All variable typing related preferences (bracket insertion, text wrapping, text wrapping pattern) can be configured at <code><a class="command" href="javascript:executeCommand('org.eclipse.ui.window.preferences(preferencePageId=org.robotframework.ide.eclipse.main.plugin.preferences.editor)')">
Window -> Preferences -> Robot Framework -> Editor</a></code> in <b>Variables</b> section.
<br/><br/><img src="images/variable_preferneces.png"/> <br/><br/>
</body>
</html>