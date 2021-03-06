<div id="accordion">
<div class="card">
    <div class="card-header">
        <h4 class="card-title"><a data-toggle="collapse" data-parent="#accordion" href="#collapseProblems">Problems</a></h4>
        <div class="card-tools">
            <span id="problems-count" data-toggle="tooltip" title="?" class="badge bg-primary">?</span>
            <button type="button" class="btn btn-tool" onclick="location.href='<%= apiRoot %>/problems'">API</button>
        </div>
    </div>
    <div id="collapseProblems" class="panel-collapse collapse in">
    <div class="card-body p-0">
        <table id="problems-table" class="table table-sm table-hover table-striped table-head-fixed">
            <thead>
                <tr>
                    <th>Id</th>
                    <th>Label</th>
                    <th>Name</th>
                    <th>Color</th>
                    <th>RGB</th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td colspan=3>
                        <div class="spinner-border"></div>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
    </div>
</div>
</div>
<script type="text/javascript">
    $(document).ready(function () {
        contest.setContestId("<%= cc.getId() %>");

        function problemTd(problem) {
            return $('<td><a href="<%= apiRoot %>/problems/' + problem.id + '">' + problem.id + '</td><td>' + problem.label
                + '</td><td>' + sanitizeHTML(problem.name) + '</td><td>' + problem.color + '</td><td>' + problem.rgb + '</td><td><div class="circle" style="background-color:' + problem.rgb + '"></div></td>');
        }

        $.when(contest.loadProblems()).done(function () {
            fillContestObjectTable("problems", contest.getProblems(), problemTd)
        }).fail(function (result) {
        	console.log("Error loading problems: " + result);
        });
    });
</script>