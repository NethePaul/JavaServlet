function post(str = "") {
    fetch("/hello", {
            body: str,
            method: "POST",
            credentials: "include"
        })
        .then(response => response.text())
        .then(function(Response) {
            eval(Response);
            nodeScriptReplace(document.getElementById("body"));
        });
};

function nodeScriptReplace(node) {
    if (nodeScriptIs(node) === true) {
        node.parentNode.replaceChild(nodeScriptClone(node), node);
    } else {
        var i = -1,
            children = node.childNodes;
        while (++i < children.length) {
            nodeScriptReplace(children[i]);
        }
    }

    return node;
};

function nodeScriptClone(node) {
    var script = document.createElement("script");
    script.text = node.innerHTML;

    var i = -1,
        attrs = node.attributes;
    while (++i < attrs.length) {
        script.setAttribute(attrs[i].name, attrs[i].value);
    }
    return script;
};

function nodeScriptIs(node) {
    return node.tagName === 'SCRIPT';
};