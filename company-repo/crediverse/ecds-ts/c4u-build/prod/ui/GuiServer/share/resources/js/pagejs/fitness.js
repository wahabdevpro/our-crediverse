updateServerFitness = function()
{
	try
	{
		var server = $("#server").val();
		$("#serverfitness").html("Retrieving health ... <img src='/img/cell_wait.gif' />");
		$("#serverfitness").load("/fitnesscheck?act=fit&server="+server);
		
	} 
	catch(err)
	{
		alert(err);
	}
};