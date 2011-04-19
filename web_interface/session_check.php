<?php
require_once ("config.inc.php");

strlen(session_id()) or session_start();
if (!isset($_SESSION["username"]))
{
    $redir="Location: https://".$_SERVER['HTTP_HOST'].dirname($_SERVER['PHP_SELF'])."/authenticate.php";
    header($redir);
    exit;
}

?>
