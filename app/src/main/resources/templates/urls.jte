@import java.util.List
@import hexlet.code.model.Url

@param List<Url> urls
@param String flash
@param String flashType

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>URLs List</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
</head>
<body>
<div class="container mt-5">
    <h1 class="mb-4">URLs</h1>

    <!-- Flash-сообщение -->
    <p>Debug: Flash = ${flash != null ? flash : "null"}, FlashType = ${flashType != null ? flashType : "null"}</p>
    @if(flash != null && !flash.isEmpty())
        <div class="alert alert-${flashType} alert-dismissible fade show" role="alert">
            ${flash}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    @endif

    <!-- Отладочная информация -->
    <p>Debug: URLs size = ${String.valueOf(urls != null ? urls.size() : "null")}, content = ${String.valueOf(urls != null ? urls : "null")}</p>

    <table class="table table-striped">
        <thead>
        <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Created At</th>
            <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        @if(urls == null || urls.isEmpty())
            <tr>
                <td colspan="4" class="text-center">No URLs found</td>
            </tr>
        @else
            @for(Url url : urls)
                <tr>
                    <td>${url.getId()}</td>
                    <td><a href="/urls/${url.getId()}">${url.getName()}</a></td>
                    <td>${new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(url.getCreatedAt())}</td>
                    <td>
                        <a href="/urls/${url.getId()}" class="btn btn-sm btn-primary">View</a>
                    </td>
                </tr>
            @endfor
        @endif
        </tbody>
    </table>
    <a href="/" class="btn btn-secondary">Back to Home</a>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
</body>
</html>