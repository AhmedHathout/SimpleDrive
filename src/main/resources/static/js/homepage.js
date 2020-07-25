function copyShareableLink(shareableLink) {
    const el = document.createElement('textarea');
    el.value = shareableLink;
    document.body.appendChild(el);
    el.select();
    document.execCommand('copy');
    document.body.removeChild(el);
    alert("Link Copied")
}

function openShareWithForm(formId) {
    alert("form opened")
    document.getElementById(formId).style.display = "block";
}

function closeShareWithForm(formId) {
    document.getElementById(formId).style.display = "none";
}

$(function () {
    $('[data-toggle="tooltip"]').tooltip()
})