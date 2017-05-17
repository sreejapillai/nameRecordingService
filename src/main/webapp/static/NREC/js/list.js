;(function($){
    var NREC = {
        ondomready:function() {
            NREC.spinner();
            NREC.getList();
            NREC.saveNote();
            NREC.addNote();
            NREC.tooltip();
        },
        spinner: function(){
            // setup spinner animation options
            $.fn.spin = function(opts) {
                this.each(function() {
                    var $this = $(this),
                        spinner = $this.data('spinner');
                    if (spinner) spinner.stop();
                    if (opts !== false) {
                      opts = $.extend({color: $this.css('color')}, opts);
                      spinner = new Spinner(opts).spin(this);
                      $this.data('spinner', spinner);
                    }
                });
                return this;
            };

            // bind spinner to ajax doc events
            $(document).on({
                ajaxStart: function() {
                    var el = $('<div class="spinner">').appendTo('body').spin();
                    $(".overlay").fadeIn().append(el);
                    var opts = {
                      lines: 12, 
                      length: 5, 
                      width: 5, 
                      radius: 10, 
                      color: '#000', 
                      speed: 1, 
                      trail: 66, 
                      shadow: false 
                    };
                    $(el).spin(opts);
                },
                ajaxStop: function() { 
                    var el = $('.spinner');
                    //el.spin(false).remove();
                    $(".overlay").fadeOut();
                }   
            });

        },
        getList: function(){

            // get the data
            $.ajax({
                type : 'GET',
                dataType : 'json',
                cache: false,
                url: '/nameRecordingService/svc/report/studentList',
                success : function(data, textStatus, jqXHR) {
                    
                    //console.log("data returned"+data);

               
                    var students = [];
                    $.each(data, function(index, obj){
                        students.push({
                            personId: obj.personId,
                            firstName: obj.firstName,
                            middleName: obj.middleName,
                            lastName: obj.lastName,
                            studentType: obj.studentType,
                            classSection: obj.classSection,
                            estimatedGradDate: obj.estimatedGradDate,
                            nameRecordingUrl: obj.nameRecordingUrl,
                            recordingNote: obj.recordingNote,
                            countryOfOrigin: obj.countryOfOrigin
                        }); 
                    });

                    // build, compile and render template 
                    var theTemplateScript = $("#listTmpl").html();
                    var theTemplate = Handlebars.compile(theTemplateScript);
                    var theCompiledHtml = theTemplate(students);
                    $('#recordings tbody').html(theCompiledHtml);

                    // set up the table
          
                    if ( $('#recordings').length > 0 ) {
                        var $dTable = $('#recordings').dataTable({
                            pageLength: 100,
                            responsive: {
                                details: {
                                    type: 'column',
                                    target: -1
                                }
                            },
                            columnDefs: [
                                { responsivePriority: 1, targets: 0, orderable: false, },  // notes bttn
                                { responsivePriority: 4, targets: 1 }, // first name
                                { responsivePriority: 2, targets: 2 }, // last name
                                { responsivePriority: 5, targets: 3 }, // type
                                { responsivePriority: 6, targets: 4 }, // section
                                { responsivePriority: 7, targets: 5 }, // graduation date
                                { responsivePriority: 8, targets: 6 }, // country
                                { responsivePriority: 3, targets: 7, orderable: false },  // audio control
                                { className: 'control', orderable: false, targets: -1 }   // responsive bttn
                            ],
                            dom: "<'row'<'col-xs-10 text-xs-left'f><'col-xs-2 text-xs-right'l>>" + "<'row'<'col-xs-12'tr>>" + "<'row text-sm-left text-xs-center'<'col-sm-6 col-xs-12 small'i><'col-sm-6 col-xs-12'p>>",
                            "oLanguage": {
                                sSearch: "",
                                sSearchPlaceholder: "Filter records",
                                sLengthMenu: "_MENU_"
                            },

                            "fnDrawCallback": function ( oSettings ) {
                                if ( oSettings.aiDisplay.length == 0 )
                                {
                                    return;
                                }
                                 
                                var nTrs = $('#recordings tbody tr');
                                var iColspan = nTrs[0].getElementsByTagName('td').length;
                                var sLastGroup = "";
                                for ( var i=0 ; i<nTrs.length ; i++ )
                                {
                                    var iDisplayIndex = oSettings._iDisplayStart + i;
                                    var sGroup = oSettings.aoData[ oSettings.aiDisplay[iDisplayIndex] ]._aData[4];
                                    if ( sGroup != sLastGroup )
                                    {
                                        var nGroup = document.createElement( 'tr' );
                                        var nCell = document.createElement( 'td' );
                                        nCell.colSpan = iColspan;
                                        nCell.className = "group table-inverse text-xs-center";
                                        nCell.innerHTML = 'Section '+sGroup;
                                        nGroup.appendChild( nCell );
                                        nTrs[i].parentNode.insertBefore( nGroup, nTrs[i] );
                                        sLastGroup = sGroup;
                                    }
                                }
                            },
                            "initComplete": function () {

                                var api = this.api();

                                $('.filter-section').click(function(){

                                    swal({
                                      title: 'Filter by Section',
                                      input: 'select',
                                      inputClass: 'form-control',
                                      inputOptions: {
                                        '*': 'Show All',
                                        'A': 'Section A',
                                        'B': 'Section B',
                                        'C': 'Section C',
                                        'D': 'Section D',
                                        'E': 'Section E',
                                        'F': 'Section F',
                                        'G': 'Section G',
                                        'H': 'Section H',
                                        'I': 'Section I',
                                        'J': 'Section J'
                                      },
                                      inputPlaceholder: 'Select Section',
                                      showCancelButton: true,
                                      inputValidator: function (value) {

                                        return new Promise(function (resolve, reject) {
                                          if (value !== '*') {
                                            api.column(4).search(value).draw();
                                            resolve();
                                          } else {
                                            api.search('').columns().search('').draw();
                                            resolve();
                                          }
                                        })
                                      }
                                    }).then(function (result) {
                                        if(result !== '*') {
                                            $('.filter-section')
                                            .html( '<i class="fa fa-filter d-inline"></i> Showing Section: ' + result)
                                            .addClass( 'active');
                                        } else {
                                            $('.filter-section')
                                            .html( '<i class="fa fa-filter d-inline"></i> Filter by Section')
                                            .removeClass( 'active');
                                        }
                                        
                                    }).catch(swal.noop);

                                });

                            }

                        });


                        $('div.dataTables_filter input').focus();
                    }

                    // instantiate tooltips
                    $('a[rel="tooltip"]').tooltip();

                },
                error: function(jqXHR, textStatus, errorThrown) {
                    if(textStatus === 'parsererror'){
                        
                        var redrectUrl = '/nameRecordingService/svc/report/list';

                        swal({
                            title: "Authentication Error",
                            html: "You're not logged in or your session has expired.<br>Redirecting to the login page in 3 seconds...",
                            type: "error",
                            timer: 3000,
                        }).then(function() {
                            // handles OK
                            window.location = redrectUrl;
                        }).catch(function(){
                            // handles timer promise
                            window.location = redrectUrl;
                        });
                        
                    }

                } 
            });

        },
        saveNote: function(){
            // intercept submit event and POST via ajax
            $(document).on('submit', '#note form', function(e){

                var data = {
                    "personId" : $(this).find('input[name="personId"]').val(),
                    "recordingNote" : $(this).find('textarea').val() 
                };

                e.preventDefault();
                $.ajax({
                    type : 'POST',
                    data: JSON.stringify(data),
                    contentType: "application/json",
                    url: $(this).attr("action"),
                    success : function(data) {

                        $("#note").modal('hide');
                        var $currRec = $('table tr td a[data-personid="'+data.personId+'"]');
                        if(data.recordingNote !== '') {
                            $currRec
                                .removeClass('btn-secondary')
                                .addClass('btn-primary')
                                .find('i')
                                .removeClass('fa-plus')
                                .addClass('fa-edit');
                            $currRec.data('note', data.recordingNote).attr('data-note', data.recordingNote);
                            $currRec.attr('title', data.recordingNote).attr('data-original-title', data.recordingNote);

                            // highlight affected row for 1 sec
                            $currRec.closest('tr').addClass('table-info');
                            setTimeout(function(){
                                $currRec.closest('tr').removeClass('table-info', 2000);
                            }, 1000);

                        } 
                        if (data.recordingNote == null || data.recordingNote == '') {
                            $currRec
                            .removeClass('btn-primary')
                            .addClass('btn-secondary')
                            .find('i')
                            .removeClass('fa-edit')
                            .addClass('fa-plus');;
                        }
                       
                    },
                    fail : function(data) {
                        swal(
                                'Error',
                                textStatus,
                                'error'
                            )
                    }
                });
            });
        },
        addNote: function(){
            // wire click event to modal
            $(document).on('click','.add-note', function(e){
                e.preventDefault();
                $("#note textarea").val( $(this).data('note') );
                $('#note input[name="personId"]').val( $(this).data('personid').toString() );
                $("#note").modal('show');        
                
                // autofocus textarea 
                $('#note').on('shown.bs.modal', function () {
                    $("#note textarea").focus();
                });       

                // display max length
                $('input[maxlength], textarea[maxlength]').maxlength({
                    alwaysShow: true,
                    threshold: 10,
                    warningClass: "tag tag-success",
                    limitReachedClass: "tag tag-danger"
                });

                $('#note').on('hide.bs.modal', function () {
                    $(".bootstrap-maxlength").hide();
                }); 
         
            });
        },
        tooltip: function(){
            // bind tooltips to datatable pagination event
            $(document).on('click','ul.pagination .page-item', function(){
                $('a[rel="tooltip"]').tooltip();
            });
        },
        last: ''
    }

    $(document).ready(NREC.ondomready);
    window.NREC = NREC;
    
})(jQuery)
