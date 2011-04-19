
<?php
    $redir="Location: https://".$_SERVER['HTTP_HOST'].dirname($_SERVER['PHP_SELF'])."/menu.php";
    header($redir);
    exit;
?>
