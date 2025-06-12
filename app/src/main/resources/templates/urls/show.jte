@import hexlet.code.model.Url
@import java.util.List
@import hexlet.code.model.UrlCheck

@param Url url
@param List<UrlCheck> checks
@param String flash
@param String flashType

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>URL Details</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
</head>
<body>
<div class="container mt-5">
    <h1 class="mb-4">URL Details</h1>

    @if(flash != null && !flash.isEmpty())
        <div class="alert alert-${flashType} alert-dismissible fade show" role="alert">
            ${flash}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    @endif

    <table class="table table-bordered">
        <tr>
            <th>ID</th>
            <td>${url.getId()}</td>
        </tr>
        <tr>
            <th>Name</th>
            <td>${url.getName()}</td>
        </tr>
        <tr>
            <th>Created At</th>
            <td>${String.valueOf(url.getCreatedAt())}</td>
        </tr>
    </table>

    <form action="/urls/${url.getId()}/checks" method="post">
        <button type="submit" class="btn btn-primary mb-3">Run check</button>
    </form>

    @if(!checks.isEmpty())
        <h2 class="mb-3">Checks</h2>
        <table class="table table-striped">
            <thead>
            <tr>
                <th>ID</th>
                <th>Status Code</th>
                <th>Title</th>
                <th>H1</th>
                <th>Description</th>
                <th>Created At</th>
            </tr>
            </thead>
            <tbody>
            @for(var check : checks)
                <tr>
                    <td>${check.getId()}</td>
                    <td>${check.getStatusCode()}</td>
                    <td>${check.getTitle()}</td>
                    <td>${check.getH1()}</td>
                    <td>${check.getDescription()}</td>
                    <td>${String.valueOf(check.getCreatedAt())}</td>
                </tr>
            @endfor
            </tbody>
        </table>
    @endif

    <a href="/urls" class="btn btn-secondary">Back to URLs</a>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
</body>
</html>