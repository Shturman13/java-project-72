@import java.util.List
@import hexlet.code.model.Url
@import java.util.Map
@import hexlet.code.model.UrlCheck

@param List<Url> urls
@param Map<Long, UrlCheck> lastChecks
@param String flash
@param String flashType

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>URLs List</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
</head>
<body>
<div class="container mt-5">
    <h1 class="mb-4">URLs</h1>

    @if(flash != null && !flash.isEmpty())
        <div class="alert alert-${flashType} alert-dismissible fade show" role="alert">
            ${flash}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    @endif

    <table class="table table-striped">
        <thead>
        <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Created At</th>
            <th>Last Check</th>
            <th>Status Code</th>
        </tr>
        </thead>
        <tbody>
        @if(urls.isEmpty())
            <tr>
                <td colspan="5" class="text-center">No URLs found</td>
            </tr>
        @else
            @for(Url url : urls)
                <tr>
                    <td>${url.getId()}</td>
                    <td><a href="/urls/${url.getId()}">${url.getName()}</a></td>
                    <td>${String.valueOf(url.getCreatedAt())}</td>
                    <td>
                        @if(lastChecks.get(url.getId()) != null)
                            ${String.valueOf(lastChecks.get(url.getId()).getCreatedAt())}
                        @else
                            -
                        @endif
                    </td>
                    <td>
                        @if(lastChecks.get(url.getId()) != null)
                            ${lastChecks.get(url.getId()).getStatusCode()}
                        @else
                            -
                        @endif
                    </td>
                </tr>
            @endfor
        @endif
        </tbody>
    </table>
    <a href="/" class="btn btn-secondary">Back to Home</a>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
</body>
</html>