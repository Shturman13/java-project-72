@import hexlet.code.model.Url

@param Url url
@param String flash
@param String flashType

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>URL Details</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
</head>
<body>
<div class="container mt-5">
    <h1 class="mb-4">URL Details</h1>

    <!-- Flash-сообщение -->
    @if(flash != null && !flash.isEmpty())
        <div class="alert alert-${flashType} alert-dismissible fade show" role="alert">
            ${flash}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    @endif

    @if(url == null)
        <p class="text-danger">URL not found.</p>
    @else
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
                <td>${new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(url.getCreatedAt())}</td>
            </tr>
        </table>
    @endif
    <a href="/urls" class="btn btn-secondary">Back to URLs</a>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
</body>
</html>
