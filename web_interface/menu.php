<?php
require_once ("config.inc.php");
require_once ("session_check.php");

try
{
    $dbh = new PDO($siteCfg->db['db_engine'] . ':host=' . $siteCfg->db['db_server'] . ';dbname=productivity',
                   $siteCfg->db['db_user'], $siteCfg->db['db_pass'],
                   array(PDO::ATTR_PERSISTENT => true));
    $dbh->exec("SET CHARACTER SET utf8");
    $dbh->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
}
catch (PDOException $e)
{
    print "Error: " . $e->getMessage() . "<br/>";
    die();
}

?>

<html>
<head>
<title>
<?php
echo "Productivity reports [" . $_SESSION["username"] . "]";
?>
</title>
</head>
<body>
<span><?php echo $_SESSION["username"]?><a href="authenticate.php?logout=yes">[Log out]</a></span><br/>

<?php
    try
    {
        $sql = "SELECT t1.timestamp, t1.name, t1.title, t2.name, t3.name FROM activities as t1 LEFT JOIN (tasks as t2, users as t3) ON (t2.id=t1.task_id AND t3.id=t1.user_id)";
        $result = $dbh->query($sql);
        $columns = $result->columnCount();

        /*foreach(range(0, $columns - 1) as $ind) {
            $meta = $result->getColumnMeta($ind);
            print_r("\t\t" . $meta['name'] . "<br/>\n");
            }*/

        print "<table>\n";
        foreach($result as $row) {
            print "\t<tr>\n";
            foreach(range(0, $columns - 1) as $ind)
                print_r("\t\t<td>" . $row[$ind] . "</td>\n");
            print "\t</tr>\n";
        }
        print "</table>\n";
    }
    catch (PDOException $e)
    {
        print "Error: " . $e->getMessage() . "<br/>";
        die();
    }
?>

</body>
</html>

    <?php $dbh = null; ?>
